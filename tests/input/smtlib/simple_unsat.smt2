; Simple UNSAT test: a = b, b = c, but a != c
; Expected: UNSAT (contradiction)

(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)

(assert (= a b))
(assert (= b c))
(assert (not (= a c)))

(check-sat)
