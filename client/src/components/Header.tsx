import React from "react";
import { Link } from "react-router-dom";
import logo from "assets/openliberty_logo.png";

const Header = () => {
  const routes = [
    { id: 1, to: "/", name: "Home" },
    { id: 2, to: "/play", name: "Play" },
    { id: 3, to: "/leaderboard", name: "Leaderboard" },
    { id: 4, to: "/settings", name: "Settings" },
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
            className="mx-5 text-gray-50 hover:text-gray-400 last:ml-auto last:mr-0"
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
