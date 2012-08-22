(defproject four "0.1.0"
  :dependencies [[org.clojure/clojure "1.5.0-alpha3"]]
  :profiles {:dev {:dependencies [[net.sf.proguard/proguard-base "4.8"]]}}
  :plugins [[lein-swank "1.4.4"]]
  :jar-name "four.jar"
  :jar-exclusions [#"project.clj"]
  :main four)
