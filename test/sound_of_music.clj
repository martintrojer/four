(ns sound-of-music
  (import [javax.sound.sampled AudioSystem AudioFormat SourceDataLine]))

(def sample-rate 44100)
(def buffer-size (/ sample-rate 50))
(def buffer (byte-array buffer-size))
(def sixteen-bit-mono (AudioFormat. sample-rate 16 1 true true))
(def ^SourceDataLine line-out (AudioSystem/getSourceDataLine sixteen-bit-mono))

(defn frequency [offset-from-a4]
  (* 440.0 (Math/pow 2 (/ offset-from-a4 12.0))))

(defn midi-note [midi]
  (frequency (- midi 69)))

(def notes [:a :a# :b :c :c# :d :d# :e :f :f# :g :g#])

(defn note [[n oct]]
  (frequency (+ (.indexOf notes n) (* 12 (- oct 4)))))

(defn clip [^double sample]
  (min 1.0 (max -1.0 sample)))

(defn sixteen-bit [^double sample]
  (long (Math/round (* sample 32767.0))))

(defn period [^double hz]
  (/ sample-rate hz))

(defn phase [^double period ^long t]
  (/ t period))

(defn sin-osc [^double hz ^long t]
  (let [period (period hz)
        angle (* (phase period t) 2.0 Math/PI)]
    (Math/sin angle)))

(defn write-sample-byte [^"[B" buffer ^long offset ^long sample]
  (let [high (unchecked-byte (- (bit-and (bit-shift-right sample 8) 0xFF) 128))
        low (unchecked-byte (- (bit-and sample 0xFF) 128))]
    (aset-byte buffer (int offset) high)
    (aset-byte buffer (int (inc offset)) low)
    buffer))

(defn write-sample-buffer [^"[B" buffer samples]
  (loop [i 0
         samples samples]
    (when samples
      (write-sample-byte buffer i (first samples))
      (recur (+ i 2) (next samples))))
  [buffer (* 2 (count samples))])

(defn out [[buffer available]]
  (.write line-out buffer 0 available))

(defn play
  ([] (play [:a 4]))
  ([[n oct]] (play [n oct] 1.0))
  ([[n oct] length] (play [n oct] length sin-osc))
  ([[n oct] length osc]
     (let [length (long (* length sample-rate))]
       (->> (iterate inc 0)
            (map (juxt identity (partial osc (note [n oct]))))
            (map (comp sixteen-bit clip second))
            (take length)
            (partition (/ buffer-size 2))
            (map (comp out (partial write-sample-buffer buffer)))
            dorun))))

(defn start []
  (.open line-out sixteen-bit-mono)
  (.start line-out))

(defn stop []
  (.stop line-out)
  (.close line-out))
