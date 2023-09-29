/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { PropsWithChildren } from "react";
import Header from "./Header";
import Footer from "./Footer";

import stars1 from "assets/stars1.png";
import stars2 from "assets/stars2.png";
import stars3 from "assets/stars3.png";
import stars4 from "assets/stars4.png";
import stars5 from "assets/stars5.png";
import { ReactComponent as Spaceship } from "assets/spaceship.svg";

const Layout = ({ children }: PropsWithChildren<{}>) => {
  return (
    <div className="font-orbitron flex flex-col min-h-screen bg-gradient-to-b from-black to-blue-dark">
      <Header />
      <main className="flex-grow z-10 py-10">
        {children}
      </main>
      <Footer />
      
      <img className="absolute top-[40%] right-[10%] animate-blink-3s" src={stars1} />
      <img className="absolute top-[15%] right-[40%] animate-blink-4s" src={stars2} />
      <img className="absolute top-[10%] right-[70%] animate-blink-5s" src={stars3} />
      <img className="absolute top-[55%] right-[85%] animate-blink-6s" src={stars4} />
      <img className="absolute top-[30%] right-[80%] animate-blink-7s" src={stars5} />
      <Spaceship className="absolute top-[55%] right-[15%]" />
    </div>
  );
};

export default Layout;
