/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        dashBg: '#13111C',       
        cardBg: '#1C1A27',       
        accentPurple: '#7C3AED', 
        successGreen: '#4ADE80', 
        dangerRed: '#F87171',    
        warningYellow: '#FBBF24' 
      }
    },
  },
  plugins: [],
}