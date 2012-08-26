(ns minigl
  (require [clojure.string :as s]
           [clojure.walk :as w])
  (import [java.awt Frame]
          [java.awt.event KeyListener KeyEvent]
          [javax.media.opengl GLProfile GLCapabilities GL2 GLContext GLEventListener]
          [javax.media.opengl.glu.gl2 GLUgl2]
          [javax.media.opengl.awt GLCanvas]))

(defn ^:private constants [prefix c]
  (->> (.getFields c)
       (map (fn [f]
              [(keyword (-> (.getName f) s/lower-case
                            (s/replace "_" "-")
                            (subs (count prefix))))
               (symbol (.getName c) (.getName f))]))
       (into {})))

(def gl-constants (constants "GL_" GL2))

(defn gl-constant [c]
  (if-let [c (gl-constants c)]
    (eval c)
    c))

(defn bits [& bits]
  (apply bit-or (map gl-constant bits)))

(defn ^:private uncamel [s]
  (s/lower-case (s/replace s #"([a-z])([A-Z])" "$1-$2")))

(defn ^:private gl-methods [prefix c]
  (->> (.getMethods c)
       (map (fn [m]
              [(-> (.getName m)
                   (subs (count prefix))
                   uncamel
                   symbol)
               m]))
       (into {})))

(defn gl-context []
  (-> (GLContext/getCurrent) .getGL .getGL2))

(defmacro ^:private alias-methods [prefix target c]
  `(do
     ~@(for [[n m] (gl-methods prefix (resolve c))
             :when (not ('#{flush class} n))
             :let [args (->> m .getParameterTypes count range
                             (map #(symbol (str "x" %))) vec)]]
         (do
           `(defn ~n ~args
              (let [~args (w/postwalk gl-constant ~args)]
                (. ~target ~(symbol (.getName m)) ~@args)))))))

(alias-methods "gl" (gl-context) GL2)
(alias-methods "glu" (GLUgl2.) GLUgl2)

(defmacro begin [what & body]
  `(try
     (.glBegin (gl-context) ~(gl-constants what))
     ~@body
     (finally
      (.glEnd (gl-context)))))

(defmacro triangles [& body]
  `(begin :triangles ~@body))

(defmacro quads [& body]
  `(begin :quads ~@body))

(defn canvas []
  (-> (GLProfile/getDefault) GLCapabilities. GLCanvas.))

(def ^:private exit-listener
  (proxy [KeyListener] []
    (keyPressed [e]
      (when (= KeyEvent/VK_ESCAPE (.getKeyCode e))
        (-> e .getComponent .getParent (.setVisible false))
        (when-not (resolve 'swank.swank/start-repl)
          (System/exit 0))))))

(defn show [renderer & {:keys [w h title] :or {w 640 h 480 title "MiniGL"}}]
  (doto (Frame. title)
    (.add (doto (canvas)
            (.addGLEventListener renderer)
            (.addKeyListener exit-listener)))
    (.setSize w h)
    (.setResizable false)
    .show))