import { motion } from 'framer-motion';
import { FiArrowDown, FiGithub, FiStar } from 'react-icons/fi';

export default function Hero() {
  return (
    <section className="relative min-h-screen flex flex-col px-6 pt-16 overflow-hidden">
      <div className="absolute inset-0 opacity-[0.03]" style={{
        backgroundImage: `linear-gradient(#FFFFFF 1px, transparent 1px), linear-gradient(90deg, #FFFFFF 1px, transparent 1px)`,
        backgroundSize: '60px 60px'
      }} />

      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-px h-32 bg-gradient-to-b from-axiom-red to-transparent" />

      {/* Main content - centered */}
      <div className="flex-1 flex flex-col items-center justify-center">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
          className="relative z-10 flex flex-col items-center text-center max-w-4xl"
        >
          <div className="mb-6 px-4 py-1.5 border border-axiom-border font-mono text-[10px] tracking-[0.3em] uppercase text-axiom-gray-muted">
            v0.1.2-beta.2
          </div>

          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
          >
            <img src="/axiom-logo.png" alt="axiom." className="w-24 h-24 object-contain" />
          </motion.div>

          <h1 className="mt-6 text-6xl md:text-8xl font-ndot tracking-tight text-axiom-white">
            a<span className="text-axiom-red">x</span>i<span className="text-axiom-red">o</span>m<span className="text-axiom-red">.</span>
          </h1>

          <p className="mt-3 font-ndot text-sm tracking-[0.3em] uppercase text-axiom-gray-muted">
            Modern design. Pure sound. Fully yours.
          </p>

          <p className="mt-4 text-base text-axiom-gray-muted max-w-xl leading-relaxed">
            A clean, fast, Material-driven music player for Android. Forked from BoomingMusic, built with precision.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row gap-4">
            <a
              href="https://github.com/RA-L-PH/axiom/releases/latest"
              target="_blank"
              rel="noopener noreferrer"
              className="group flex items-center gap-3 px-8 py-4 bg-axiom-red text-axiom-white font-ndot text-sm tracking-widest uppercase hover:bg-axiom-red-dark transition-all"
            >
              Download
              <span className="inline-block w-4 h-px bg-axiom-white group-hover:w-6 transition-all" />
            </a>
            <a
              href="https://github.com/RA-L-PH/axiom"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-3 px-8 py-4 border border-axiom-border text-axiom-white font-ndot text-sm tracking-widest uppercase hover:border-axiom-white transition-colors"
            >
              <FiGithub size={16} />
              Source Code
            </a>
          </div>

          <div className="mt-8 flex items-center gap-8 font-ndot text-xs tracking-widest text-axiom-gray-muted">
            <div className="flex items-center gap-2">
              <FiStar size={12} className="text-axiom-red" />
              <span>Open Source</span>
            </div>
            <div className="w-px h-3 bg-axiom-border" />
            <span>GPL-3.0</span>
            <div className="w-px h-3 bg-axiom-border" />
            <span>Android</span>
          </div>
        </motion.div>
      </div>

      {/* Scroll indicator - fixed at bottom of viewport */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.2 }}
        className="pb-6 flex flex-col items-center gap-2"
      >
        <span className="font-ndot text-[10px] tracking-[0.3em] uppercase text-axiom-gray-muted">
          Scroll
        </span>
        <motion.div
          animate={{ y: [0, 6, 0] }}
          transition={{ repeat: Infinity, duration: 1.5 }}
        >
          <FiArrowDown size={14} className="text-axiom-red" />
        </motion.div>
      </motion.div>
    </section>
  );
}
