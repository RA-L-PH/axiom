import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { FiX } from 'react-icons/fi';

const baseUrl = 'https://raw.githubusercontent.com/RA-L-PH/axiom/master/assets';

const screenshots = [
  { label: 'For You', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.05%20(1).jpeg' },
  { label: 'Songs', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.05.jpeg' },
  { label: 'Albums', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.06%20(1).jpeg' },
  { label: 'Album View', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.06.jpeg' },
  { label: 'Search', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.07%20(1).jpeg' },
  { label: 'Player', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.07%20(2).jpeg' },
  { label: 'Lyrics', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.07.jpeg' },
  { label: 'Queue', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.08%20(1).jpeg' },
  { label: 'Equalizer', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.08%20(2).jpeg' },
  { label: 'Options', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.08%20(3).jpeg' },
  { label: 'Settings', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.08%20(4).jpeg' },
  { label: 'Tags', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.08.jpeg' },
  { label: 'Sleep Timer', file: 'WhatsApp%20Image%202026-08-15%20at%2013.51.09.jpeg' },
];

export default function Screenshots() {
  const [selected, setSelected] = useState<number | null>(null);

  return (
    <section id="screenshots" className="py-24 px-6 bg-axiom-gray">
      <div className="max-w-6xl mx-auto">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Screenshots
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <div className="text-center mb-16">
          <h3 className="text-3xl md:text-4xl font-light text-axiom-white font-space">
            See it in action.
          </h3>
          <p className="mt-3 text-axiom-gray-muted max-w-lg mx-auto font-space">
            Clean interfaces. Zero distractions. Pure music.
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {screenshots.map((s, i) => (
            <motion.div
              key={s.label}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.04 }}
              onClick={() => setSelected(i)}
              className="group relative bg-axiom-black border border-axiom-border overflow-hidden cursor-pointer hover:border-axiom-red transition-colors"
            >
              <div className="aspect-[9/16]">
                <img
                  src={`${baseUrl}/${s.file}`}
                  alt={s.label}
                  className="w-full h-full object-cover"
                  loading="lazy"
                />
              </div>
            </motion.div>
          ))}
        </div>
      </div>

      {/* Lightbox */}
      <AnimatePresence>
        {selected !== null && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setSelected(null)}
            className="fixed inset-0 z-50 bg-axiom-black/95 flex items-center justify-center p-6 cursor-pointer"
          >
            <button
              onClick={() => setSelected(null)}
              className="absolute top-6 right-6 p-3 border border-axiom-border text-axiom-white hover:border-axiom-red hover:text-axiom-red transition-colors"
            >
              <FiX size={20} />
            </button>
            <motion.img
              key={selected}
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              src={`${baseUrl}/${screenshots[selected].file}`}
              alt={screenshots[selected].label}
              className="max-h-[85vh] max-w-[90vw] object-contain border border-axiom-border"
              onClick={(e) => e.stopPropagation()}
            />
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  );
}
