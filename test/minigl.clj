(ns minigl
  (require [clojure.string :as s]
           [clojure.walk :as w])
  (import [java.awt Frame]
          [java.awt.event KeyListener KeyEvent]
          [javax.media.opengl GLProfile GLCapabilities GL2 GLContext GLEventListener]
          [javax.media.opengl.glu.gl2 GLUgl2]
          [javax.media.opengl.awt GLCanvas])
  (:refer-clojure :exclude [flush])
  )


(defn ^:private constants [prefix c]
  (->> (.getFields c)
       (map (fn [f]
              [(keyword (-> (.getName f) s/lower-case
                            (s/replace "_" "-")
                            (subs (count prefix))))
               (symbol (.getName c) (.getName f))]))
       (into {})))

(defn ^:private unprefixed-methods [pre c]
  (->>  (.getMethods c)
        (map #(subs (.getName %) (count pre)))
        set))

(def ^:private gl2-methods (unprefixed-methods "gl" GL2))
(def ^:private glu-methods (unprefixed-methods "glu" GLUgl2))

(defn ^:private constants [prefix c]
  (->> (.getFields c)
       (map (fn [f]
              [(keyword (-> (.getName f) s/lower-case
                            (s/replace "_" "-")
                            (subs (count prefix))))
               (symbol (.getName c) (.getName f))]))
       (into {})))

(defn ^:private camel [s]
  (->> (re-seq #"[^-]+" (str s))
       (map (fn [[c & cs]] (apply str (s/upper-case c) cs)))
       (apply str)))

(def gl-constants (constants "GL_" GL2))

(defn gl-context []
  (-> (GLContext/getCurrent) .getGL .getGL2))

(defmacro gl [& body]
  `(try
     ~@(for [[m & args :as expr] body]
         (let [cm (camel m)
               args (w/postwalk #(get gl-constants % %) args)]
           (condp some [cm]
             gl2-methods `(~(symbol (str ".gl" cm)) (gl-context) ~@args)
             glu-methods `(~(symbol (str ".glu" cm)) (GLUgl2.) ~@args)
             expr)))
    (finally
     (.glFlush (gl-context)))))

(defmacro begin [what & body]
  `(try
     (.glBegin (gl-context) ~(gl-constants what))
     (gl ~@body)
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