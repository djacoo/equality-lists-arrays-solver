#!/usr/bin/env python3
"""
Analyze experiment results and generate statistics.

Phase 4.2 of the project plan - Experiments
"""

import csv
import sys
from pathlib import Path
from collections import defaultdict
from typing import Dict, List

def load_results(csv_file: Path) -> List[Dict]:
    """Load results from CSV file."""
    results = []
    with open(csv_file, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            results.append(row)
    return results

def analyze_by_category(results: List[Dict]) -> Dict:
    """Analyze results grouped by category."""
    by_category = defaultdict(lambda: {
        'total': 0,
        'correct': 0,
        'incorrect': 0,
        'errors': 0,
        'sat': 0,
        'unsat': 0,
        'total_time': 0.0,
        'min_time': float('inf'),
        'max_time': 0.0,
        'total_literals': 0,
        'total_functions': 0
    })

    for result in results:
        cat = result['category']
        stats = by_category[cat]

        stats['total'] += 1

        # Correctness
        if result['correct'] == 'True':
            stats['correct'] += 1
        elif result['correct'] == 'False':
            stats['incorrect'] += 1

        # Errors
        if result['success'] == 'False' or result['result'] in ['ERROR', 'TIMEOUT']:
            stats['errors'] += 1

        # SAT/UNSAT
        if result['result'] == 'SAT':
            stats['sat'] += 1
        elif result['result'] == 'UNSAT':
            stats['unsat'] += 1

        # Timing
        if result['wall_time_ms']:
            time_ms = float(result['wall_time_ms'])
            stats['total_time'] += time_ms
            stats['min_time'] = min(stats['min_time'], time_ms)
            stats['max_time'] = max(stats['max_time'], time_ms)

        # Problem size
        if result['literals']:
            stats['total_literals'] += int(result['literals'])
        if result['functions']:
            stats['total_functions'] += int(result['functions'])

    # Calculate averages
    for cat, stats in by_category.items():
        if stats['total'] > 0:
            stats['avg_time'] = stats['total_time'] / stats['total']
            stats['avg_literals'] = stats['total_literals'] / stats['total']
            stats['avg_functions'] = stats['total_functions'] / stats['total']

    return dict(by_category)

def print_summary(results: List[Dict], by_category: Dict):
    """Print summary statistics."""

    print("=" * 80)
    print("EXPERIMENT RESULTS SUMMARY")
    print("=" * 80)
    print()

    # Overall statistics
    total = len(results)
    correct = sum(1 for r in results if r['correct'] == 'True')
    incorrect = sum(1 for r in results if r['correct'] == 'False')
    errors = sum(1 for r in results if r['success'] == 'False' or r['result'] in ['ERROR', 'TIMEOUT'])
    sat = sum(1 for r in results if r['result'] == 'SAT')
    unsat = sum(1 for r in results if r['result'] == 'UNSAT')

    print("OVERALL STATISTICS")
    print("-" * 80)
    print(f"  Total test cases:     {total}")
    print(f"  Correct results:      {correct} ({correct/total*100:.1f}%)")
    print(f"  Incorrect results:    {incorrect} ({incorrect/total*100:.1f}%)")
    print(f"  Errors/Timeouts:      {errors} ({errors/total*100:.1f}%)")
    print(f"  SAT results:          {sat}")
    print(f"  UNSAT results:        {unsat}")
    print()

    # Timing statistics
    times = [float(r['wall_time_ms']) for r in results if r['wall_time_ms']]
    if times:
        avg_time = sum(times) / len(times)
        min_time = min(times)
        max_time = max(times)
        print("TIMING STATISTICS")
        print("-" * 80)
        print(f"  Average runtime:      {avg_time:.3f} ms")
        print(f"  Minimum runtime:      {min_time:.3f} ms")
        print(f"  Maximum runtime:      {max_time:.3f} ms")
        print(f"  Total runtime:        {sum(times):.3f} ms")
        print()

    # Category breakdown
    print("RESULTS BY CATEGORY")
    print("-" * 80)
    print(f"{'Category':<15} {'Tests':<8} {'Correct':<10} {'SAT':<8} {'UNSAT':<8} {'Avg Time (ms)':<15}")
    print("-" * 80)

    for cat in sorted(by_category.keys()):
        stats = by_category[cat]
        correct_pct = f"{stats['correct']}/{stats['total']} ({stats['correct']/stats['total']*100:.0f}%)"
        avg_time = stats.get('avg_time', 0)

        print(f"{cat:<15} {stats['total']:<8} {correct_pct:<10} {stats['sat']:<8} {stats['unsat']:<8} {avg_time:<15.3f}")

    print()

    # Problem size statistics
    print("PROBLEM SIZE STATISTICS")
    print("-" * 80)
    print(f"{'Category':<15} {'Avg Literals':<15} {'Avg Functions':<15}")
    print("-" * 80)

    for cat in sorted(by_category.keys()):
        stats = by_category[cat]
        print(f"{cat:<15} {stats.get('avg_literals', 0):<15.1f} {stats.get('avg_functions', 0):<15.1f}")

    print()

    # Failed tests
    failed = [r for r in results if r['correct'] == 'False']
    if failed:
        print("FAILED TESTS")
        print("-" * 80)
        for r in failed:
            print(f"  {r['category']}/{r['filename']}")
            print(f"    Expected: {r['expected']}, Got: {r['result']}")
        print()

    # Errors
    error_results = [r for r in results if r['success'] == 'False' or r['result'] in ['ERROR', 'TIMEOUT']]
    if error_results:
        print("ERRORS/TIMEOUTS")
        print("-" * 80)
        for r in error_results:
            print(f"  {r['category']}/{r['filename']}")
            print(f"    Result: {r['result']}")
            if r['error']:
                print(f"    Error: {r['error']}")
        print()

    print("=" * 80)

def main():
    """Main entry point."""
    script_dir = Path(__file__).parent
    results_file = script_dir / 'results.csv'

    if not results_file.exists():
        print(f"ERROR: Results file not found: {results_file}")
        print("Please run run_experiments.py first.")
        sys.exit(1)

    # Load and analyze results
    results = load_results(results_file)
    by_category = analyze_by_category(results)

    # Print summary
    print_summary(results, by_category)

if __name__ == '__main__':
    main()
