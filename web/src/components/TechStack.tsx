import { motion } from 'framer-motion';
import {
  SiKotlin,
  SiJetpackcompose,
  SiGradle,
  SiFirebase,
  SiSpotify,
  SiMaterialdesign,
} from 'react-icons/si';
import { FiCpu, FiDatabase, FiLayers, FiZap } from 'react-icons/fi';

const stack = [
  { icon: SiKotlin, name: 'Kotlin', role: 'Language' },
  { icon: SiJetpackcompose, name: 'Jetpack Compose', role: 'UI Framework' },
  { icon: SiMaterialdesign, name: 'Material 3', role: 'Design System' },
  { icon: FiCpu, name: 'Media3 ExoPlayer', role: 'Audio Engine' },
  { icon: SiFirebase, name: 'Room DB', role: 'Persistence' },
  { icon: SiGradle, name: 'Gradle KTS', role: 'Build System' },
  { icon: FiLayers, name: 'MVVM + Repository', role: 'Architecture' },
  { icon: FiZap, name: 'Coroutines + Flow', role: 'Async' },
  { icon: FiDatabase, name: 'Koin', role: 'Dependency Injection' },
  { icon: SiSpotify, name: 'Coil', role: 'Image Loading' },
];

const techStack = [
  { layer: 'Audio Engine', tech: 'Media3 ExoPlayer' },
  { layer: 'Architecture', tech: 'MVVM + Repository Pattern' },
  { layer: 'Persistence', tech: 'Room Database' },
  { layer: 'DI', tech: 'Koin' },
  { layer: 'Async', tech: 'Coroutines & Flow' },
  { layer: 'UI', tech: 'Compose + Views (Hybrid)' },
  { layer: 'Images', tech: 'Coil' },
  { layer: 'Design', tech: 'Material 3 / Material You' },
  { layer: 'Language', tech: 'Kotlin' },
];

export default function TechStack() {
  return (
    <section id="tech" className="py-24 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Tech Stack
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <div className="text-center mb-16">
          <h3 className="text-3xl md:text-4xl font-light text-axiom-white font-space">
            Built on solid foundations.
          </h3>
          <p className="mt-3 text-axiom-gray-muted max-w-lg mx-auto font-space">
            Modern Android development with battle-tested libraries.
          </p>
        </div>

        <motion.div
          initial="hidden"
          whileInView="show"
          viewport={{ once: true }}
          variants={{
            hidden: {},
            show: { transition: { staggerChildren: 0.05 } },
          }}
          className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-px bg-axiom-border mb-16"
        >
          {stack.map((s) => (
            <motion.div
              key={s.name}
              variants={{ hidden: { opacity: 0 }, show: { opacity: 1 } }}
              className="group bg-axiom-black p-6 flex flex-col items-center gap-3 hover:bg-axiom-gray transition-colors"
            >
              <s.icon
                size={28}
                className="text-axiom-gray-muted group-hover:text-axiom-red transition-colors"
              />
              <span className="font-ndot text-[10px] tracking-wider uppercase text-axiom-white text-center">
                {s.name}
              </span>
              <span className="font-ntype-mono text-[9px] tracking-wider uppercase text-axiom-gray-muted">
                {s.role}
              </span>
            </motion.div>
          ))}
        </motion.div>

        <div className="border border-axiom-border">
          <div className="grid grid-cols-2 bg-axiom-gray">
            <div className="px-6 py-3 font-ndot text-[10px] tracking-[0.2em] uppercase text-axiom-red border-r border-axiom-border">
              Layer
            </div>
            <div className="px-6 py-3 font-ndot text-[10px] tracking-[0.2em] uppercase text-axiom-red">
              Technology
            </div>
          </div>
          {techStack.map((t, i) => (
            <div
              key={t.layer}
              className={`grid grid-cols-2 ${i % 2 === 0 ? 'bg-axiom-black' : 'bg-axiom-gray'} border-t border-axiom-border`}
            >
              <div className="px-6 py-3 font-ntype-mono text-xs text-axiom-gray-muted border-r border-axiom-border">
                {t.layer}
              </div>
              <div className="px-6 py-3 font-space text-xs text-axiom-white">
                {t.tech}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
