(ns nehe-lesson-2
  (use minigl)
  (import [javax.media.opengl GLEventListener]))

(def renderer
  (proxy [GLEventListener] []
    (init [_]
      (clear-color 0 0 0 0)
      (shade-model :flat))

    (display [_]
      (clear (bits :color-buffer-bit :depth-buffer-bit))
      (load-identity)
      (translated -1.5 0 -6)
      (triangles
       (vertex3d 0 1 0)
       (vertex3d -1 -1 0)
       (vertex3d 1 -1 0))
      (translated 3 0 0)
      (quads
       (vertex3d -1 1 0)
       (vertex3d 1 1 0)
       (vertex3d 1 -1 0)
       (vertex3d -1 -1 0)))

    (reshape [_ x y w h]
      (let [h (if (pos? h) h 1)]
        (viewport 0 0 w h)
        (matrix-mode :projection)
        (load-identity)
        (perspective 45.0 (/ w h 1.0) 1.0 20.0)
        (matrix-mode :modelview)
        (load-identity)))))

(defn -main [& args]
  (show renderer :title "Nehe Lesson 2"))
