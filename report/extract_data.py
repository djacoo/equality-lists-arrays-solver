#!/usr/bin/env python3
"""
Extract experimental data from results.csv for LaTeX tables
This script processes the experimental results and generates
LaTeX table rows that can be copied into report.tex
"""

import csv
import statistics
from collections import defaultdict

def load_results(csv_path):
    """Load results from CSV file"""
    results = []
    with open(csv_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            results.append(row)
    return results

def analyze_by_category(results):
    """Analyze results grouped by category"""
    categories = defaultdict(list)

    for row in results:
        category = row['category']
        categories[category].append(row)

    stats = {}
    for category, rows in categories.items():
        total = len(rows)
        correct = sum(1 for r in rows if r['correct'] == 'True')
        accuracy = (correct / total * 100) if total > 0 else 0

        # Runtime statistics
        solver_times = [float(r['solver_time_ms']) for r in rows if r['solver_time_ms']]

        stats[category] = {
            'total': total,
            'correct': correct,
            'accuracy': accuracy,
            'avg_time': statistics.mean(solver_times) if solver_times else 0,
            'min_time': min(solver_times) if solver_times else 0,
            'max_time': max(solver_times) if solver_times else 0,
            'std_time': statistics.stdev(solver_times) if len(solver_times) > 1 else 0
        }

    return stats

def print_correctness_table(stats):
    """Print LaTeX table rows for correctness results"""
    print("\n=== CORRECTNESS TABLE (Table 1) ===")
    print("Copy these rows into Table 1 in report.tex:\n")

    # Map categories to nice names
    category_names = {
        'te': r'$\mathcal{T}_E$',
        'tcons': r'$\mathcal{T}_{cons}$',
        'tarray': r'$\mathcal{T}_A$',
        'combined': 'Combined',
        'smtlib': 'SMT-LIB'
    }

    # Print rows for each category
    total_tests = 0
    total_correct = 0

    for cat in ['te', 'tcons', 'tarray', 'combined', 'smtlib']:
        if cat in stats:
            s = stats[cat]
            total_tests += s['total']
            total_correct += s['correct']
            print(f"{category_names[cat]} & {s['total']} & {s['correct']} & {s['accuracy']:.1f} \\\\")

    # Overall row
    overall_accuracy = (total_correct / total_tests * 100) if total_tests > 0 else 0
    print("\\midrule")
    print(f"\\textbf{{Overall}} & {total_tests} & {total_correct} & {overall_accuracy:.1f} \\\\")

def print_performance_table(stats):
    """Print LaTeX table rows for performance results"""
    print("\n=== PERFORMANCE TABLE (Table 2) ===")
    print("Copy these rows into Table 2 in report.tex:\n")

    category_names = {
        'te': r'$\mathcal{T}_E$',
        'tcons': r'$\mathcal{T}_{cons}$',
        'tarray': r'$\mathcal{T}_A$',
        'combined': 'Combined',
        'smtlib': 'SMT-LIB'
    }

    all_times = []
    total_tests = 0

    for cat in ['te', 'tcons', 'tarray', 'combined', 'smtlib']:
        if cat in stats:
            s = stats[cat]
            total_tests += s['total']
            all_times.extend([s['avg_time']] * s['total'])  # Weighted average
            print(f"{category_names[cat]} & {s['total']} & {s['avg_time']:.2f} & "
                  f"{s['min_time']:.2f} & {s['max_time']:.2f} & {s['std_time']:.2f} \\\\")

    # Overall row
    overall_avg = statistics.mean(all_times) if all_times else 0
    overall_std = statistics.stdev(all_times) if len(all_times) > 1 else 0
    print("\\midrule")
    print(f"\\textbf{{Overall}} & {total_tests} & {overall_avg:.2f} & "
          f"{min(all_times):.2f} & {max(all_times):.2f} & {overall_std:.2f} \\\\")

def print_summary_stats(results):
    """Print general summary statistics"""
    print("\n=== SUMMARY STATISTICS ===")
    print(f"Total test cases: {len(results)}")

    correct = sum(1 for r in results if r['correct'] == 'True')
    print(f"Correct: {correct}/{len(results)} ({correct/len(results)*100:.1f}%)")

    sat_count = sum(1 for r in results if r['result'] == 'SAT')
    unsat_count = sum(1 for r in results if r['result'] == 'UNSAT')
    print(f"SAT results: {sat_count}")
    print(f"UNSAT results: {unsat_count}")

    solver_times = [float(r['solver_time_ms']) for r in results if r['solver_time_ms']]
    if solver_times:
        print(f"\nRuntime statistics (solver time):")
        print(f"  Average: {statistics.mean(solver_times):.2f} ms")
        print(f"  Median: {statistics.median(solver_times):.2f} ms")
        print(f"  Min: {min(solver_times):.2f} ms")
        print(f"  Max: {max(solver_times):.2f} ms")
        print(f"  Std Dev: {statistics.stdev(solver_times):.2f} ms")

    # Failed tests
    failed = [r for r in results if r['correct'] == 'False']
    if failed:
        print(f"\n=== FAILED TESTS ({len(failed)}) ===")
        for r in failed:
            print(f"  {r['filename']}: Expected {r['expected']}, Got {r['result']}")

def print_abstract_data(results):
    """Print data for filling in the abstract"""
    print("\n=== DATA FOR ABSTRACT ===")
    correct = sum(1 for r in results if r['correct'] == 'True')
    accuracy = correct / len(results) * 100

    solver_times = [float(r['solver_time_ms']) for r in results if r['solver_time_ms']]
    avg_time = statistics.mean(solver_times) if solver_times else 0

    print(f"Correctness rate: {accuracy:.1f}%")
    print(f"Average runtime: {avg_time:.2f} ms")
    print("\nSuggested abstract text:")
    print(f"The experimental results demonstrate {accuracy:.1f}% correctness rate "
          f"with average runtime of {avg_time:.2f} ms across {len(results)} test cases.")

if __name__ == '__main__':
    import sys
    import os

    # Determine CSV path
    if len(sys.argv) > 1:
        csv_path = sys.argv[1]
    else:
        # Default: ../experiments/results.csv
        script_dir = os.path.dirname(os.path.abspath(__file__))
        csv_path = os.path.join(script_dir, '..', 'experiments', 'results.csv')

    if not os.path.exists(csv_path):
        print(f"Error: Results file not found: {csv_path}")
        print("\nUsage: python3 extract_data.py [path/to/results.csv]")
        sys.exit(1)

    print(f"Loading results from: {csv_path}\n")
    results = load_results(csv_path)
    stats = analyze_by_category(results)

    print_abstract_data(results)
    print_summary_stats(results)
    print_correctness_table(stats)
    print_performance_table(stats)

    print("\n" + "="*60)
    print("Copy the table rows above into your report.tex file")
    print("="*60)
