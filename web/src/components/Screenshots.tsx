import { motion } from 'framer-motion';
import { FiExternalLink } from 'react-icons/fi';

const screenshots = [
  { label: 'For You', color: '#D71921' },
  { label: 'Songs', color: '#1A1A1A' },
  { label: 'Albums', color: '#000000' },
  { label: 'Album View', color: '#2A2A2A' },
  { label: 'Search', color: '#1A1A1A' },
  { label: 'Player', color: '#D71921' },
  { label: 'Lyrics', color: '#000000' },
  { label: 'Equalizer', color: '#2A2A2A' },
];

export default function Screenshots() {
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

        <div className="grid grid-cols-2 md:grid-cols-4 gap-px bg-axiom-border">
          {screenshots.map((s, i) => (
            <motion.div
              key={s.label}
              initial={{ opacity: 0, scale: 0.95 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.05 }}
              className="group relative aspect-[9/16] bg-axiom-black overflow-hidden cursor-pointer"
            >
              <div className="absolute inset-0 flex flex-col items-center justify-center gap-3">
                <div
                  className="w-16 h-16 border border-axiom-border flex items-center justify-center"
                  style={{ backgroundColor: s.color }}
                >
                  <span className="font-ntype-mono text-[10px] text-axiom-gray-muted">
                    [{String(i + 1).padStart(2, '0')}]
                  </span>
                </div>
                <span className="font-ndot text-[10px] tracking-widest uppercase text-axiom-gray-muted">
                  {s.label}
                </span>
              </div>

              <div className="absolute inset-0 bg-axiom-red/10 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <FiExternalLink size={20} className="text-axiom-red" />
              </div>

              <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-axiom-red scale-x-0 group-hover:scale-x-100 transition-transform origin-left" />
            </motion.div>
          ))}
        </div>

        <p className="mt-6 text-center font-ntype-mono text-[10px] tracking-widest text-axiom-gray-muted uppercase">
          Screenshots from v0.1.2-beta.2 — placeholders until assets are added
        </p>
      </div>
    </section>
  );
}
