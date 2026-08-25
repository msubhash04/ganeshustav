/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,jsx}',
  ],
  theme: {
    extend: {
      colors: {
        saffron: {
          50: '#fff8ed',
          100: '#ffefd1',
          200: '#ffdba3',
          300: '#ffc16a',
          400: '#ff9f33',
          500: '#ff9933', // primary saffron
          600: '#f2760f',
          700: '#c95a0c',
          800: '#a04711',
          900: '#833c12',
        },
        maroon: {
          50: '#fdf3f2',
          100: '#fbe1de',
          200: '#f6c0ba',
          300: '#ec938a',
          400: '#dd5f52',
          500: '#c43d31',
          600: '#a52a20',
          700: '#7a1f18', // deep red accent
          800: '#5c1712',
          900: '#3d0f0c',
        },
        gold: {
          400: '#e8c468',
          500: '#d4af37', // gold accent
          600: '#b3921f',
        },
        cream: '#fff8f0',
      },
      fontFamily: {
        display: ['"Poppins"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
      },
      boxShadow: {
        card: '0 2px 10px rgba(122, 31, 24, 0.08)',
      },
    },
  },
  plugins: [],
}
