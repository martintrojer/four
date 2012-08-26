(defproject four "0.1.0"
  :dependencies [[org.clojure/clojure "1.5.0-alpha4"]
                 [org.clojars.biallym/jogl.all "2.0-rc9"]
                 [org.clojars.biallym/jogl-native-all "2.0-rc9"]
                 [org.clojars.biallym/gluegen-rt "2.0-rc9"]]
  :profiles {:dev {:dependencies [[net.sf.proguard/proguard-base "4.8"]]}}
  :plugins [[lein-swank "1.4.4"]]
  :jar-name "four.jar"
  :jar-exclusions [#"project.clj" #"maven" #"leiningen"]
  :jvm-opts ["-Dsun.java2d.noddraw=true" "-Dsun.java2d.opengl=false"]
  :main four)
