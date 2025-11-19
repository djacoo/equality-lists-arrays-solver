; List theory test using cons, car, cdr
; car(cons(x,y)) = x should be SAT
; Expected: SAT

(set-logic QF_UF)
(declare-fun cons (Int Int) Int)
(declare-fun car (Int) Int)
(declare-fun cdr (Int) Int)
(declare-fun x () Int)
(declare-fun y () Int)

(assert (= (car (cons x y)) x))

(check-sat)
