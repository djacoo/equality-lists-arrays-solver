#!/bin/bash
# Script to set GitHub repository metadata
# Run this script to update repository description, topics, and settings

REPO_OWNER="djacoo"
REPO_NAME="equality-lists-arrays-solver"

# Repository description
DESCRIPTION="Decision procedure solver for the union of theories: Equality (T_E), Lists (T_cons), and Arrays (T_A). Implements congruence closure algorithm from Bradley & Manna. Academic project for PAR course."

# Repository topics
TOPICS=(
  "automated-reasoning"
  "decision-procedures"
  "congruence-closure"
  "satisfiability"
  "smt-solver"
  "java"
  "theory-solver"
  "formal-verification"
  "arrays"
  "lists"
  "equality"
  "bradley-manna"
  "academic-project"
  "university-verona"
)

echo "============================================"
echo "GitHub Repository Metadata Configuration"
echo "============================================"
echo ""
echo "Repository: $REPO_OWNER/$REPO_NAME"
echo ""
echo "Description:"
echo "  $DESCRIPTION"
echo ""
echo "Topics:"
for topic in "${TOPICS[@]}"; do
  echo "  - $topic"
done
echo ""
echo "============================================"
echo ""
echo "To apply these settings using GitHub CLI:"
echo ""
echo "# Set description"
echo "gh repo edit $REPO_OWNER/$REPO_NAME --description \"$DESCRIPTION\""
echo ""
echo "# Set topics (one command)"
echo "gh repo edit $REPO_OWNER/$REPO_NAME --add-topic $(IFS=, ; echo "${TOPICS[*]}")"
echo ""
echo "# Enable features"
echo "gh repo edit $REPO_OWNER/$REPO_NAME --enable-issues --enable-wiki=false --enable-projects"
echo ""
echo "# Set homepage (optional)"
echo "gh repo edit $REPO_OWNER/$REPO_NAME --homepage https://github.com/$REPO_OWNER/$REPO_NAME"
echo ""
echo "============================================"

