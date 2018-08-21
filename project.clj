(defproject lxsli/picomock "0.1.12"
  :description "Simple mocking helper library"
  :url "https://github.com/lxsli/picomock"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}}

  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]]
  :lein-release {:deploy-via :clojars}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version" "patch"]
                  ["vcs" "commit"]
                  ["vcs" "push" "origin" "master"]])
