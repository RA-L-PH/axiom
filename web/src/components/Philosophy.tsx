import { motion } from 'framer-motion';
import { FiSliders, FiInstagram, FiCpu } from 'react-icons/fi';

export default function Philosophy() {
  return (
    <section id="philosophy" className="py-24 px-6 bg-axiom-black border-t border-b border-axiom-border relative overflow-hidden">
      {/* Background radial glow */}
      <div className="absolute top-1/2 left-1/4 -translate-y-1/2 w-96 h-96 bg-axiom-red/[0.03] rounded-full blur-[150px] pointer-events-none" />

      <div className="max-w-6xl mx-auto relative z-10">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Philosophy
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left Block - Title and intro */}
          <div className="lg:col-span-5 space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 border border-axiom-border bg-axiom-gray/30 rounded-none">
              <FiSliders className="text-axiom-red animate-pulse" size={14} />
              <span className="font-mono text-[10px] tracking-widest text-axiom-gray-muted uppercase">Design Fusion</span>
            </div>
            <h3 className="text-3xl md:text-5xl font-light text-axiom-white font-space leading-tight">
              Where Hardware <br />
              Meets Content.
            </h3>
            <p className="text-sm md:text-base text-axiom-gray-muted leading-relaxed font-space">
              axiom is born from a simple idea: blending the hardware-inspired, digital-retro elements of <strong>Nothing Tech</strong> with a polished, <strong>Instagram Reel-like Now Playing</strong> screen. The result is a music player that feels both physical and lightweight.
            </p>
          </div>

          {/* Right Block - The Two Pillars */}
          <div className="lg:col-span-7 grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Pillar 1: Nothing OS Inspiration */}
            <motion.div 
              whileHover={{ y: -4 }}
              transition={{ duration: 0.3 }}
              className="p-8 bg-axiom-gray/20 border border-axiom-border hover:border-axiom-red/30 transition-colors relative group"
            >
              <div className="absolute top-0 left-0 w-full h-[2px] bg-axiom-red scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left" />
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2.5 bg-axiom-black border border-axiom-border text-axiom-white">
                  <FiCpu size={18} />
                </div>
                <h4 className="font-ndot text-sm tracking-wider uppercase text-axiom-white">
                  Nothing Tech
                </h4>
              </div>
              <p className="text-sm text-axiom-gray-muted leading-relaxed font-space">
                Raw, industrial typography and strict monochrome layouts. We borrow Nothing's iconic dot-matrix design, strict hardware red accents, and tactile widgets to make interactions feel mechanical and deliberate.
              </p>
            </motion.div>

            {/* Pillar 2: Instagram Reel Inspiration */}
            <motion.div 
              whileHover={{ y: -4 }}
              transition={{ duration: 0.3 }}
              className="p-8 bg-axiom-gray/20 border border-axiom-border hover:border-axiom-red/30 transition-colors relative group"
            >
              <div className="absolute top-0 left-0 w-full h-[2px] bg-axiom-red scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left" />
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2.5 bg-axiom-black border border-axiom-border text-axiom-white">
                  <FiInstagram size={18} />
                </div>
                <h4 className="font-ndot text-sm tracking-wider uppercase text-axiom-white">
                  Instagram Reel UI
                </h4>
              </div>
              <p className="text-sm text-axiom-gray-muted leading-relaxed font-space">
                Clean visual hierarchy and absolute focus on cover art. The Now Playing screen is inspired by Instagram Reels, utilizing full-screen immersive media backgrounds, floating controls, and overlaying typography for a modern layout.
              </p>
            </motion.div>
          </div>
        </div>
      </div>
    </section>
  );
}
