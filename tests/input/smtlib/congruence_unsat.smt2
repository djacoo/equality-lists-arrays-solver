; Congruence closure test
; a = b, f(a) != f(b) should be UNSAT
; Expected: UNSAT (congruence violation)

(set-logic QF_UF)
(declare-fun f (Int) Int)
(declare-fun a () Int)
(declare-fun b () Int)

(assert (= a b))
(assert (not (= (f a) (f b))))

(check-sat)
