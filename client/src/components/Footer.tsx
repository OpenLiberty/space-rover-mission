import React from "react";
import builtOnLibertyBadge from "assets/openliberty_builton_badge.png";
import githubLogo from "assets/github_logo.png";
import twitterLogo from "assets/twitter_logo.png";

const Footer = () => {
  const socialLinks = [
    {
      href: "https://github.com/openliberty/space-rover-mission",
      name: "GitHub",
      logo: githubLogo,
    },
    {
      href: "https://twitter.com/OpenLibertyIO",
      name: "Twitter",
      logo: twitterLogo,
    },
  ];

  return (
    <footer className="px-10 py-5 flex items-center justify-between">
      <a
        className="inline-block"
        href="https://github.com/openliberty/open-liberty"
        target="_blank"
        rel="noopener noreferrer"
      >
        <img
          className="h-14"
          src={builtOnLibertyBadge}
          alt="Built on Open Liberty"
        />
      </a>
      <div className="flex items-center">
        {socialLinks.map((link) => (
          <a
            key={link.name}
            className="inline-block mr-5 last:mr-0"
            href={link.href}
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              className="h-14"
              src={link.logo}
              alt={`${link.name} logo`}
            />
          </a>
        ))}
      </div>
    </footer>
  );
};

export default Footer;
