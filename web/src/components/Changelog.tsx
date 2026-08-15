import { motion } from 'framer-motion';
import { FiCheck, FiAlertCircle } from 'react-icons/fi';

const changes = [
  {
    category: 'Visual & UI',
    color: 'bg-axiom-red',
    items: [
      'Migrated 147 vector icons to Material Symbols Sharp',
      'Refactored player options into 4-column grid layout',
      'Queue view now slides as 70% side sheet from right',
      'Text-filling song name seekbar with red progress fill',
    ],
  },
  {
    category: 'Backend & Stability',
    color: 'bg-axiom-white',
    items: [
      'Fixed MusicBrainz infinite hang from nested mutex calls',
      'Resolved Spotify lock inversion and integer overflow bug',
      'Switched Genius API to Ktor safe parameter builders',
      'Cleaned dead branches in scrobble/now-playing logic',
      'Added 7-day TTL to local preferences cache',
    ],
  },
];

export default function Changelog() {
  return (
    <section id="changelog" className="py-24 px-6 bg-axiom-gray">
      <div className="max-w-6xl mx-auto">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Changelog
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <div className="text-center mb-16">
          <h3 className="text-3xl md:text-4xl font-light text-axiom-white font-space">
            v0.1.2-beta.2
          </h3>
          <p className="mt-3 text-axiom-gray-muted font-space">
            Significant visual updates and backend stability fixes.
          </p>
          <div className="mt-4 inline-flex items-center gap-2 px-4 py-1.5 border border-axiom-border font-ntype-mono text-[10px] tracking-[0.2em] uppercase text-axiom-gray-muted">
            Released August 15, 2026
          </div>
        </div>

        <motion.div
          initial="hidden"
          whileInView="show"
          viewport={{ once: true }}
          variants={{
            hidden: {},
            show: { transition: { staggerChildren: 0.1 } },
          }}
          className="grid md:grid-cols-2 gap-px bg-axiom-border"
        >
          {changes.map((group) => (
            <motion.div
              key={group.category}
              variants={{
                hidden: { opacity: 0, y: 20 },
                show: { opacity: 1, y: 0 },
              }}
              className="bg-axiom-black p-8"
            >
              <div className="flex items-center gap-3 mb-6">
                <div className={`w-2 h-2 ${group.color}`} />
                <h4 className="font-ndot text-xs tracking-[0.2em] uppercase text-axiom-white">
                  {group.category}
                </h4>
              </div>
              <ul className="space-y-3">
                {group.items.map((item, i) => (
                  <li key={i} className="flex items-start gap-3">
                    {group.color === 'bg-axiom-red' ? (
                      <FiCheck size={14} className="text-axiom-red mt-0.5 shrink-0" />
                    ) : (
                      <FiAlertCircle size={14} className="text-axiom-white mt-0.5 shrink-0" />
                    )}
                    <span className="text-sm text-axiom-gray-muted leading-relaxed font-space">
                      {item}
                    </span>
                  </li>
                ))}
              </ul>
            </motion.div>
          ))}
        </motion.div>

        <div className="mt-8 p-6 border border-axiom-border text-center">
          <p className="font-ntype-mono text-xs text-axiom-gray-muted leading-relaxed">
            a<span className="text-axiom-red">x</span>i<span className="text-axiom-red">o</span>m<span className="text-axiom-red">.</span> is <span className="text-axiom-white">forked from</span>{' '}
            <a
              href="https://github.com/mardous/BoomingMusic"
              target="_blank"
              rel="noopener noreferrer"
              className="text-axiom-red hover:underline"
            >
              BoomingMusic
            </a>{' '}
            by mardous. Developed and maintained by{' '}
            <a
              href="https://github.com/RA-L-PH"
              target="_blank"
              rel="noopener noreferrer"
              className="text-axiom-red hover:underline"
            >
              RA-L-PH
            </a>
            . Licensed under GPL-3.0.
          </p>
        </div>
      </div>
    </section>
  );
}
