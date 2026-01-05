# Makefile for LaTeX report compilation

# Main document
MAIN = report
TEX = pdflatex
BIB = bibtex

# Default target
all: $(MAIN).pdf

# Compile PDF with bibliography
$(MAIN).pdf: $(MAIN).tex references.bib
	$(TEX) $(MAIN)
	$(BIB) $(MAIN)
	$(TEX) $(MAIN)
	$(TEX) $(MAIN)

# Quick compile (no bibliography update)
quick: $(MAIN).tex
	$(TEX) $(MAIN)

# Clean auxiliary files
clean:
	rm -f *.aux *.log *.out *.toc *.bbl *.blg *.lof *.lot
	rm -f *.synctex.gz *.fdb_latexmk *.fls

# Clean everything including PDF
distclean: clean
	rm -f $(MAIN).pdf

# View PDF (macOS)
view: $(MAIN).pdf
	open $(MAIN).pdf

# Continuous compilation (requires latexmk)
watch:
	latexmk -pdf -pvc $(MAIN).tex

.PHONY: all quick clean distclean view watch
