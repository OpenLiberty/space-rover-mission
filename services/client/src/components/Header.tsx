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
import logo from "assets/openliberty_logo.png";

const Header = () => {
  return (
    <header className="px-10 py-5 flex text-2xl">
      <a
        href="https://openliberty.io"
        target="_blank"
        rel="noopener noreferrer"
      >
        <img className="inline-block mr-5" src={logo} alt="Open Liberty logo" />
      </a>
    </header>
  );
};

export default Header;
