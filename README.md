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
values to return, in sequence order each time the mock is used - useful for emulating stateful dependencies

## Usage

Add this to dependencies of your project.clj:

![](https://clojars.org/audiogum/picomock/latest-version.svg)

Trivial example:

```
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

For more examples see unit tests.





