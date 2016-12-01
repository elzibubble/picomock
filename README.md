# picomock

![](https://clojars.org/audiogum/picomock/latest-version.svg)

Tiny library for mocking function dependencies. 

Feature overview:

* No dependency on testing framework, works fine with `core.test`

* Optionally check that the mock was called the right number of times and inspect
the arguments of each call (no enforced expectations mechanism)

* No re-defs: designed for cases where dependencies are passed

* Mock any function dependency, not limited to protocol implementations

* Create a mock using a function or simply a value ("always return this")

* Create a mock using a sequence of functions to be called, or sequence of
values to return, in sequence order each time the mock is used - useful for simulating stateful dependencies

## Rationale and examples

`picomock` is intended as a convenience library for unit testing `clojure` code with dependencies. Compared to other testing frameworks, there is very little here, no macros or mechanisms to reach "under the hood" and replace pieces of code. `picomock` is designed to support and encourage a coding style where external dependencies and state are kept decoupled from the logic of the application so that as much of the codebase as possible is "pure" and therefore unit testable. This implies dependencies are explicitly passed, supporting simple mocking techniques. For example this may be via `protocol` implementations or simply passing functions.

In this trivial example, we provide a simple `mock`, checking that the dependency is called and that the correct arguments were passed:

```clojure
(ns mytests
  (:require [picomock.core :refer [mock mock-calls mock-args]])
  
(defn function-under-test
  "I just call the function I'm given passing it some values"
  [f]
  (f 2 3))
  
(deftest function-under-test-works
  (let [mymock (mock (fn [a b] (* a b)))]
    (is (= 6
           (function-under-test mymock)))
    (is (= 1
           (mock-calls mymock)))
    (is (= '(2 3)
           (first (mock-args mymock))))))
```

`picomock` helps particularly where your unit under test depends on stateful dependencies - for example data stores or external services. Sequences of values or functions can be provided for cases where the test expects to use the dependency multiple times and get different results. When done, to assert correct behaviour `picomock` allows you to check how many times the mock was called and inspect the arguments of each call. 

In this example, we use `mockvals` to simulate state change by providing a sequence of returned values. For more complex stateful cases, `mock` can be used instead, providing a sequence of functions to call. 

```clojure
(defn call-me-until-stop
  "Calls f creating sequence of values until f returns :stop"
  [f]
  (loop [s []]
    (let [nextresult (f)]
      (if (= :stop nextresult)
        s
        (recur (conj s nextresult))))))
        
(deftest call-me-until-stop-works
  (let [mockf (mockvals [:a :b :c :stop])]
    (is (= [:a :b :c]
           (call-me-until-stop mockf)))
    (is (= 4
           (mock-calls mockf)))))
```

For more examples see [unit tests](https://github.com/audiogum/picomock/blob/master/test/picomock/unit/core.clj)





