#!/usr/bin/env python3
"""
Experiment Runner for Equality, Lists, and Arrays Solver
Runs all test cases and collects performance metrics.

Phase 4.2 of the project plan - Experiments
"""

import subprocess
import time
import os
import re
import csv
from pathlib import Path
from typing import Dict, List, Tuple
import sys

# ANSI color code remover
ANSI_ESCAPE = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')

def remove_ansi_codes(text: str) -> str:
    """Remove ANSI color codes from text."""
    return ANSI_ESCAPE.sub('', text)

def get_test_files(base_path: Path) -> List[Tuple[str, Path]]:
    """Get all test files organized by category."""
    test_files = []

    # Custom format tests
    for category in ['te', 'tcons', 'tarray', 'combined']:
        category_path = base_path / 'input' / category
        if category_path.exists():
            for test_file in sorted(category_path.glob('*.txt')):
                test_files.append((category, test_file))

    # SMT-LIB tests
    smtlib_path = base_path / 'input' / 'smtlib'
    if smtlib_path.exists():
        for test_file in sorted(smtlib_path.glob('*.smt2')):
            test_files.append(('smtlib', test_file))

    return test_files

def extract_expected_result(test_file: Path) -> str:
    """Extract expected result from filename or file header."""
    filename = test_file.stem.lower()

    if '_sat' in filename and '_unsat' not in filename:
        return 'SAT'
    elif '_unsat' in filename:
        return 'UNSAT'

    # Try to read from file header
    try:
        with open(test_file, 'r') as f:
            for line in f:
                line = line.strip().lower()
                if 'expected: sat' in line:
                    return 'SAT'
                elif 'expected: unsat' in line:
                    return 'UNSAT'
    except:
        pass

    return 'UNKNOWN'

def get_problem_size(test_file: Path) -> Dict[str, int]:
    """Calculate problem size metrics from test file."""
    literal_count = 0
    term_count = 0
    function_count = 0

    try:
        with open(test_file, 'r') as f:
            for line in f:
                line = line.strip()
                # Skip comments and empty lines
                if not line or line.startswith('#') or line.startswith(';'):
                    continue

                # Count literals (lines with = or !=)
                if '=' in line or '!=' in line:
                    literal_count += 1

                # Count function applications
                function_count += line.count('(')

    except:
        pass

    return {
        'literals': literal_count,
        'functions': function_count
    }

def run_solver(jar_path: Path, test_file: Path, is_smtlib: bool = False) -> Dict:
    """Run the solver on a test file and collect results."""

    # Determine which main class to use
    main_class = "solver.SMTLIBSolver" if is_smtlib else "solver.Main"

    # Prepare command - use -cp and explicit main class for correct parser invocation
    cmd = ['java', '-cp', str(jar_path), main_class, str(test_file)]

    # Run solver and measure time
    start_time = time.perf_counter()
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30  # 30 second timeout
        )
        end_time = time.perf_counter()

        output = result.stdout
        error = result.stderr
        return_code = result.returncode

    except subprocess.TimeoutExpired:
        end_time = time.perf_counter()
        return {
            'result': 'TIMEOUT',
            'runtime_ms': (end_time - start_time) * 1000,
            'solver_time_ms': None,
            'success': False,
            'error': 'Timeout after 30 seconds'
        }
    except Exception as e:
        return {
            'result': 'ERROR',
            'runtime_ms': None,
            'solver_time_ms': None,
            'success': False,
            'error': str(e)
        }

    # Calculate wall-clock time
    wall_time_ms = (end_time - start_time) * 1000

    # Parse output
    clean_output = remove_ansi_codes(output)

    # Extract result (SAT/UNSAT)
    result_match = re.search(r'Result:\s*(SAT|UNSAT)', clean_output)
    result_str = result_match.group(1) if result_match else 'UNKNOWN'

    # Extract solver's reported time
    time_match = re.search(r'Time:\s*([\d.]+)\s*ms', clean_output)
    solver_time_ms = float(time_match.group(1)) if time_match else None

    return {
        'result': result_str,
        'runtime_ms': wall_time_ms,
        'solver_time_ms': solver_time_ms,
        'success': return_code == 0,
        'error': error if error else None
    }

def run_experiments(project_root: Path, output_file: Path):
    """Run all experiments and save results to CSV."""

    tests_path = project_root / 'tests'
    jar_path = project_root / 'target' / 'equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar'

    if not jar_path.exists():
        print(f"ERROR: JAR file not found at {jar_path}")
        print("Please run 'mvn package' first.")
        sys.exit(1)

    # Get all test files
    test_files = get_test_files(tests_path)

    print(f"Found {len(test_files)} test files")
    print(f"JAR: {jar_path}")
    print(f"Output: {output_file}")
    print("-" * 80)

    # Open CSV file for writing
    with open(output_file, 'w', newline='') as csvfile:
        fieldnames = [
            'test_id',
            'category',
            'filename',
            'expected',
            'result',
            'correct',
            'wall_time_ms',
            'solver_time_ms',
            'literals',
            'functions',
            'success',
            'error'
        ]
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()

        # Run each test
        for idx, (category, test_file) in enumerate(test_files, 1):
            test_name = test_file.name
            is_smtlib = category == 'smtlib'

            print(f"[{idx}/{len(test_files)}] Running {category}/{test_name}...", end=' ')
            sys.stdout.flush()

            # Get expected result and problem size
            expected = extract_expected_result(test_file)
            size = get_problem_size(test_file)

            # Run solver
            result = run_solver(jar_path, test_file, is_smtlib)

            # Check correctness
            correct = (result['result'] == expected) if expected != 'UNKNOWN' else None

            # Write result
            writer.writerow({
                'test_id': idx,
                'category': category,
                'filename': test_name,
                'expected': expected,
                'result': result['result'],
                'correct': correct,
                'wall_time_ms': f"{result['runtime_ms']:.3f}" if result['runtime_ms'] else None,
                'solver_time_ms': f"{result['solver_time_ms']:.3f}" if result['solver_time_ms'] else None,
                'literals': size['literals'],
                'functions': size['functions'],
                'success': result['success'],
                'error': result['error'] if result['error'] else ''
            })

            # Print result
            status = '✓' if correct else '✗' if correct is False else '?'
            print(f"{status} {result['result']} ({result['runtime_ms']:.2f}ms)" if result['runtime_ms'] else f"✗ {result['result']}")

    print("-" * 80)
    print(f"Results saved to {output_file}")

def main():
    """Main entry point."""
    # Determine project root (parent of experiments directory)
    script_dir = Path(__file__).parent
    project_root = script_dir.parent

    # Output file
    output_file = script_dir / 'results.csv'

    print("=" * 80)
    print("Equality, Lists, and Arrays Solver - Experiment Runner")
    print("Phase 4.2: Testing & Experimentation")
    print("=" * 80)
    print()

    run_experiments(project_root, output_file)

    print("\nExperiment completed successfully!")
    print(f"Results: {output_file}")

if __name__ == '__main__':
    main()
