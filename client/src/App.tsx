import React from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import Layout from "components/Layout";
import HomePage from "pages/HomePage";

const App = () => {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
};

export default App;
