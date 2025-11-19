; Function application test: f(a,b) = a
; Expected: SAT

(set-logic QF_UF)
(declare-fun f (Int Int) Int)
(declare-fun a () Int)
(declare-fun b () Int)

(assert (= (f a b) a))

(check-sat)
