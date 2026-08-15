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

        <motion.div
          variants={container}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true, margin: '-100px' }}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-px bg-axiom-border"
        >
          {features.map((f) => (
            <motion.div
              key={f.title}
              variants={item}
              className="group bg-axiom-black p-8 hover:bg-axiom-gray transition-colors"
            >
              <div className="flex items-center gap-3 mb-4">
                <f.icon
                  size={18}
                  className="text-axiom-red group-hover:scale-110 transition-transform"
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
      </div>
    </section>
  );
}
