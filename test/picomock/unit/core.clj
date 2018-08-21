(ns picomock.unit.core
  (:require [picomock.core :refer :all]
            [clojure.test :refer :all]))

;; example 0: a mock that captures calls and always returns nil to callers

(deftest mock-with-no-fn-works-as-spy
  (let [mymock (mock)]
    (testing "the mock returns expected result"
      (is (nil? (mymock 2 3))))
    (testing "the mock has recorded the right number of calls"
      (is (= 1 (mock-calls mymock))
          "My mock wasn't called right number of times"))
    (testing "the mock captured the right arguments"
      (is (= '(2 3)
             (first (mock-args mymock)))
          "First call to my mock had wrong args"))))

;; example 1: passing a mock to a function that takes a function argument

(defn example-function
  "I just call the function I'm given passing it some values"
  [f]
  (f 2 3))

(deftest example-function-works
  (let [mymock (mock (fn [a b] (* a b)))]
    (testing "the function under test returns expected result"
      (is (= 6
             (example-function mymock))))
    (testing "the function has called the mock the right number of times"
      (is (= 1
             (mock-calls mymock))
          "My mock wasn't called right number of times"))
    (testing "the function passed the right arguments"
      (is (= '(2 3)
             (first (mock-args mymock)))
          "First call to my mock had wrong args"))))

;; example 2: passing a mocked implementation of a protocol

(defprotocol MyProtocol
  (proto-fn-1 [_])
  (proto-fn-2 [_ a b]))

(defn use-my-protocol
  "take values and an implementation of a protocol and calculate something"
  [a b impl]
  (+ (proto-fn-1 impl)
     (proto-fn-2 impl a (+ b 2))))

(deftest use-my-protocol-works
  (let [m-proto-fn-1 (mock (fn [] 300))
        m-proto-fn-2 (mock (fn [a b] (+ a b)))
        protoimpl (reify MyProtocol
                    (proto-fn-1 [this]
                      (m-proto-fn-1))
                    (proto-fn-2 [this a b]
                      (m-proto-fn-2 a b)))]
    (testing "calling the function gives correct result given mocked dependency"
        (is (= 309
               (use-my-protocol 3 4 protoimpl))))
    (testing "the correct arguments were passed to a dependency"
      (is (= '(3 6)
             (first (mock-args m-proto-fn-2)))))))

;; example 3: multiple calls to mocks are tracked

(defn call-me-loads
  "Calls f n times with incrementing numbers creating a sequence, ignoring g"
  [n f g]
  (map f (range n)))

(deftest call-me-loads-makes-right-calls
  (let [mockf (mock (fn [x] (* 2 x)))
        mockg (mock (fn [x] (* 3 x)))]
    (is (= '(0 2 4 6 8)
           (call-me-loads 5 mockf mockg)))
    (testing "f was called 5 times"
      (is (= 5
             (mock-calls mockf))))
    (testing "g was not called at all"
      (is (= 0
             (mock-calls mockg))))
    (testing "3rd call (0 indexed) to f had expected argument"
      (is (= '(2)
             (nth (mock-args mockf) 2))))))

;; example 4: stateful dependencies: multiple calls implemented with different stub functions to mimic state change

(defn call-me-until-stop
  "Calls f creating sequence of values until f returns :stop"
  [f]
  (loop [s []]
    (let [nextresult (f)]
      (if (= :stop nextresult)
        s
        (recur (conj s nextresult))))))

(deftest call-me-until-stop-works
  (let [mockf (mock [(fn [] :a)
                     (fn [] :b)
                     (fn [] :c)
                     (fn [] :stop)])]
    (is (= [:a :b :c]
           (call-me-until-stop mockf)))
    (is (= 4
           (mock-calls mockf)))))

;; example 5: mocking by returning fixed values (similar to example 1 but with mockval)

(deftest example-function-works-2
  (let [mymock (mockval :answer)]
    (testing "the function under test returns expected result"
      (is (= :answer
             (example-function mymock))))
    (testing "the function has called the mock the right number of times"
      (is (= 1
             (mock-calls mymock))
          "My mock wasn't called right number of times"))
    (testing "the function passed the right arguments"
      (is (= '(2 3)
             (first (mock-args mymock)))
          "First call to my mock had wrong args"))))


;; example 6: mocking with sequence of values (same as example 4 but with mockvals)

(deftest call-me-until-stop-works-2
  (let [mockf (mockvals [:a :b :c :stop])]
    (is (= [:a :b :c]
           (call-me-until-stop mockf)))
    (is (= 4
           (mock-calls mockf)))))


;; example 7: mocking for async behaviour using pauses and timings

(defn call-stuff-in-futures
  [f1 f2 f3 f4]
  (let [future-f1 (future (f1))
        future-f2 (future (f2))
        r1 @future-f1
        future-f3 (future (f3)) ; mimic f3 depending on f1 finishing, but not f2
        r2 @future-f2
        r3 @future-f3
        future-f4 (future (f4)) ; mimic f4 depending on f2 and f3 finishing
        r4 @future-f4]
    [r1 r2 r3 r4]))

(deftest call-stuff-in-futures-works
  (let [mf1 (mockval :1 100)
        mf2 (mockval :2 200) ; takes longer than f1
        mf3 (mockval :3 100)
        mf4 (mockval :4 100)]
    (call-stuff-in-futures mf1 mf2 mf3 mf4)
    (testing "all mocks were called once"
      (is (= 1
             (mock-calls mf1)
             (mock-calls mf2)
             (mock-calls mf3)
             (mock-calls mf4))))
    (testing "f1 started before f3 started"
      (is (< (first (mock-starttimes mf1))
             (first (mock-starttimes mf3)))))
    (testing "f2 started no later than f3 started"
      (is (<= (first (mock-starttimes mf2))
              (first (mock-starttimes mf3)))))
    (testing "f3 started before f2 completed"
      (is (< (first (mock-starttimes mf3))
             (first (mock-completetimes mf2)))))
    (testing "f3 started no earlier than f1 completed"
      (is (>= (first (mock-starttimes mf3))
              (first (mock-completetimes mf1)))))
    (testing "f4 started no earlier than f3 and f2 completed"
      (is (>= (first (mock-starttimes mf4))
              (first (mock-completetimes mf2)))
          (>= (first (mock-starttimes mf4))
              (first (mock-completetimes mf3)))))))

;; example 8: calling `apply` on a mock

(deftest apply-mock
  (let [m (mock)]
    (apply m 1 2 [3 4])
    (testing "all args were received"
      (is (= ['(1 2 3 4)]
             (mock-args m))))))
