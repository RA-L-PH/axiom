import Navbar from './components/Navbar';
import Hero from './components/Hero';
import Features from './components/Features';
import Screenshots from './components/Screenshots';
import TechStack from './components/TechStack';
import Changelog from './components/Changelog';
import DownloadSection from './components/DownloadSection';
import Footer from './components/Footer';

export default function App() {
  return (
    <div className="min-h-screen bg-axiom-black text-axiom-white">
      <Navbar />
      <Hero />
      <Features />
      <Screenshots />
      <TechStack />
      <Changelog />
      <DownloadSection />
      <Footer />
    </div>
  );
}
