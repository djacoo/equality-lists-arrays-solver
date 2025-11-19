; Simple SAT test: a = b and b = c should be satisfiable
; Expected: SAT with a, b, c in the same equivalence class

(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)

(assert (= a b))
(assert (= b c))

(check-sat)
