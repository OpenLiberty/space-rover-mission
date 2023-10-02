/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React from "react";
import { Link } from "react-router-dom";
import logo from "assets/openliberty_logo.png";

const Header = () => {
  const routes = [
    { id: 1, to: "/", name: "Home" },
    { id: 2, to: "/play", name: "Play" },
    { id: 3, to: "/leaderboard", name: "Leaderboard" },
    // { id: 4, to: "/settings", name: "Settings" },
  ];

  return (
    <header className="px-10 py-5 flex text-2xl">
      <a
        href="https://openliberty.io"
        target="_blank"
        rel="noopener noreferrer"
      >
        <img className="inline-block mr-5" src={logo} alt="Open Liberty logo" />
      </a>
      <nav className="flex-grow flex items-center">
        {routes.map((route) => (
          <Link
            key={route.id}
            className="mx-5 text-gray-50 hover:text-gray-400"
            // TODO: uncomment below and remove above after adding settings page
            // className="mx-5 text-gray-50 hover:text-gray-400 last:ml-auto last:mr-0"
            to={route.to}
          >
            {route.name}
          </Link>
        ))}
      </nav>
    </header>
  );
};

export default Header;
