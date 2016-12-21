(defproject audiogum/picomock "0.1.10-SNAPSHOT"
  :description "Simple mocking helper library"
  :url "https://github.com/audiogum/picomock"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version" "patch"]
                  ["vcs" "commit"]
                  ["vcs" "push" "origin" "master"]])
