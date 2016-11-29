(ns picomock.core)

(defn create-call-state*
  [functions]
  (atom {:callcount 0
         :args []
         :functions functions}))

(defn update-call-state*
  [callstate args]
  (swap! callstate
         (fn [cs]
           (-> cs
               (update :args
                       (fn [a]
                         (conj a args)))
               (update :callcount
                       inc)))))

(defn invoke*
  [callstate & args]
  (let [{:keys [functions callcount]} @callstate
        f (nth (cycle functions) callcount)]
    (update-call-state* callstate args)
    (apply f args)))

(defprotocol Mock
  (mock-calls [this])
  (mock-args [this]))

(defrecord Picomock [callstate]
  Mock
  (mock-calls [this]
    (:callcount @callstate))
  (mock-args [this]
    (:args @callstate))
  clojure.lang.IFn
  (invoke [this]
    (invoke* callstate))
  (invoke [this a]
    (invoke* callstate a))
  (invoke [this a b]
    (invoke* callstate a b))
  (invoke [this a b c]
    (invoke* callstate a b c))
  (invoke [this a b c d]
    (invoke* callstate a b c d))
  (invoke [this a b c d e]
    (invoke* callstate a b c d e))
  (invoke [this a b c d e g]
    (invoke* callstate a b c d e g))
  (invoke [this a b c d e g h]
    (invoke* callstate a b c d e g h))
  (invoke [this a b c d e g h i]
    (invoke* callstate a b c d e g h i))
  (invoke [this a b c d e g h i j]
    (invoke* callstate a b c d e g h i j))
  (invoke [this a b c d e g h i j k]
    (invoke* callstate a b c d e g h i j k))
  (invoke [this a b c d e g h i j k l]
    (invoke* callstate a b c d e g h i j k l))
  (invoke [this a b c d e g h i j k l m]
    (invoke* callstate a b c d e g h i j k l m))
  (invoke [this a b c d e g h i j k l m n]
    (invoke* callstate a b c d e g h i j k l m n))
  (invoke [this a b c d e g h i j k l m n o]
    (invoke* callstate a b c d e g h i j k l m n o))
  (invoke [this a b c d e g h i j k l m n o p]
    (invoke* callstate a b c d e g h i j k l m n o p))
  (invoke [this a b c d e g h i j k l m n o p q]
    (invoke* callstate a b c d e g h i j k l m n o p q))
  (invoke [this a b c d e g h i j k l m n o p q r]
    (invoke* callstate a b c d e g h i j k l m n o p q r)))

(defn val->fn
  [v]
  (fn [& _]
    v))

(defn mock
  [f-or-fs]
  (->Picomock
   (create-call-state* (if (sequential? f-or-fs)
                         f-or-fs
                         [f-or-fs]))))

(defn mockval
  [retval]
  (->Picomock
   (create-call-state* [(val->fn retval)])))

(defn mockvals
  [retvals]
  (->Picomock
   (create-call-state* (map val->fn retvals))))
