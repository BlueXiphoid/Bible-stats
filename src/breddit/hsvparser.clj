(ns breddit.hsvparser "Parses the HTML from the Herziene Statenvertaling crawled website and and structures it."
  (:use pl.danieljanus.tagsoup))

(defn extract-chapter-body [html]
  ((((first (filter #(= (% 0) :body) html)) 2) 2) 2))

(defn chapter-html [name] (parse (str "ripped/" name ".html")))

(defn chapter-body [name] (extract-chapter-body (chapter-html name)))

(defn current-story [xml]
  (if (and (vector? xml) (= (:class (xml 1)) "s")) (xml 2)))

(defn split-by-story-title-seq [chapter-body]
  (partition-by current-story chapter-body))

(defn story-titles-seq [chapter-body]
  (filter (complement nil?)
          (map #(current-story (first %)) (split-by-story-title-seq chapter-body) )))

(defn split-seq-head-story [split-seq]
  (current-story (first (first split-seq))))

(defn story-title+contents [split-seq]
  [(split-seq-head-story split-seq) (first (next split-seq))])

(defn stories-seq [chapter-body]
  (let [parts (split-by-story-title-seq chapter-body)]
    (map story-title+contents
         (partition 2
                    (if (split-seq-head-story parts) parts
                      #_else (cons nil parts))))))

;
;WIP / Scratchpad
;


(def gen-1-body (chapter-body "Genesis 1"))

(defrecord vers [name chapter vers])

(defrecord book [title, name, stories])

(defrecord story [title, verses])

(defrecord vers-text [ref, text, literally, translators-note, related-verses])

(def sample-book (book. "HET EERSTE" "GENESIS"
                       [(story. "De schepping"
                               [(vers-text. (vers. "GEN" 1 3) "" "En" "zie ook" 
                                           [(vers. "GEN" 1 8)])])]))