import { motion } from 'framer-motion';
import {
  FiMusic,
  FiHeadphones,
  FiMic,
  FiSliders,
  FiList,
  FiClock,
  FiBluetooth,
  FiTag,
  FiRefreshCw,
  FiGlobe,
  FiBarChart2,
  FiFolder,
} from 'react-icons/fi';

const features = [
  {
    icon: FiMic,
    title: 'Auto Lyrics',
    desc: 'Automatic lyrics download, sync, and word-by-word display with translation support.',
  },
  {
    icon: FiSliders,
    title: 'Equalizer',
    desc: '15-band fully configurable EQ with AutoEq headphone correction profiles.',
  },
  {
    icon: FiMusic,
    title: 'Gapless Playback',
    desc: 'Smooth zero-interruption transitions between tracks.',
  },
  {
    icon: FiList,
    title: 'Smart Playlists',
    desc: 'Auto-generated Recently Played, Most Played, and History lists.',
  },
  {
    icon: FiBarChart2,
    title: 'Scrobbling',
    desc: 'Native Last.fm and ListenBrainz integration for listening history.',
  },
  {
    icon: FiBluetooth,
    title: 'Bluetooth Controls',
    desc: 'Full playback management via connected headsets and car systems.',
  },
  {
    icon: FiGlobe,
    title: 'Android Auto',
    desc: 'Complete hands-free music experience on the road.',
  },
  {
    icon: FiFolder,
    title: 'Folder Browsing',
    desc: 'Play songs directly from any folder with whitelist/blacklist filtering.',
  },
  {
    icon: FiClock,
    title: 'Sleep Timer',
    desc: 'Auto-stop playback after a set time period.',
  },
  {
    icon: FiTag,
    title: 'Tag Editor',
    desc: 'Edit song metadata — title, artist, album info inline.',
  },
  {
    icon: FiRefreshCw,
    title: 'ReplayGain',
    desc: 'Consistent volume normalization across all tracks.',
  },
  {
    icon: FiHeadphones,
    title: 'Widgets',
    desc: 'Lock screen and home screen controls for quick access.',
  },
];

const container = {
  hidden: {},
  show: {
    transition: {
      staggerChildren: 0.06,
    },
  },
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

export default function Features() {
  return (
    <section id="features" className="py-24 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Features
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <div className="text-center mb-16">
          <h3 className="text-3xl md:text-4xl font-light text-axiom-white font-space">
            Everything you need.
          </h3>
          <p className="mt-3 text-axiom-gray-muted max-w-lg mx-auto font-space">
            axiom packs powerful features into a clean, distraction-free interface.
          </p>
        </div>

        {/* Desktop View */}
        <motion.div
          variants={container}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true, margin: '-100px' }}
          className="hidden sm:grid sm:grid-cols-2 lg:grid-cols-3 gap-px bg-axiom-border"
        >
          {features.map((f) => (
            <motion.div
              key={f.title}
              variants={item}
              className="group bg-axiom-black p-8 hover:bg-axiom-gray/60 transition-all duration-300 relative overflow-hidden cursor-default"
            >
              <div className="absolute top-0 left-0 w-full h-[2px] bg-axiom-red scale-x-0 group-hover:scale-x-100 transition-transform duration-500 origin-left" />
              <div className="flex items-center gap-3 mb-4">
                <f.icon
                  size={18}
                  className="text-axiom-red group-hover:scale-110 transition-transform duration-300"
                />
                <h4 className="font-ndot text-sm tracking-wider uppercase text-axiom-white">
                  {f.title}
                </h4>
              </div>
              <p className="text-sm text-axiom-gray-muted leading-relaxed font-space">
                {f.desc}
              </p>
            </motion.div>
          ))}
        </motion.div>

        {/* Mobile View - Movable Cards */}
        <div className="flex sm:hidden overflow-x-auto snap-x snap-mandatory gap-4 pb-6 scrollbar-none">
          {features.map((f, idx) => (
            <div
              key={f.title}
              className="min-w-[80vw] snap-center bg-axiom-gray/40 border border-axiom-border p-6 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center gap-3 mb-4">
                  <f.icon
                    size={18}
                    className="text-axiom-red"
                  />
                  <h4 className="font-ndot text-sm tracking-wider uppercase text-axiom-white">
                    {f.title}
                  </h4>
                </div>
                <p className="text-sm text-axiom-gray-muted leading-relaxed font-space">
                  {f.desc}
                </p>
              </div>
              <div className="mt-6 flex items-center justify-between border-t border-axiom-border/50 pt-4 text-[10px] font-ntype-mono text-axiom-gray-muted uppercase tracking-widest">
                <span>Feature</span>
                <span>{String(idx + 1).padStart(2, '0')} / {String(features.length).padStart(2, '0')}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
