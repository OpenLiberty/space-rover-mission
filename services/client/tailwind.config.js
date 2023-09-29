/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        green: {
          light: "#c7ee63",
          DEFAULT: "#abd155",
          dark: "#96bc32",
        },
        blue: {
          light: "#546fb5",
          DEFAULT: "#5e6b8d",
          dark: "#2c2e50",
        },
        orange: {
          DEFAULT: "#f69144",
        },
      },
      keyframes: {
        blink: {
          "0%, 100%": {
            opacity: 0,
          },
          "10%, 40%": {
            opacity: 1,
          },
        },
      },
      animation: {
        'blink-3s': "blink 3s infinite",
        'blink-4s': "blink 4s infinite",
        'blink-5s': "blink 5s infinite",
        'blink-6s': "blink 6s infinite",
        'blink-7s': "blink 7s infinite"
      }
    },
    fontFamily: {
      orbitron: "Orbitron",
    },
  },
  plugins: [],
};
