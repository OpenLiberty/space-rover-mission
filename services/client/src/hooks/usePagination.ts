/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect } from "react";

const ITEMS_PER_PAGE = 5;

const usePagination = (data: any[]) => {
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(
    Math.max(Math.ceil(data.length / ITEMS_PER_PAGE), 1)
  );
  const [paginatedData, setPaginatedData] = useState(
    data.slice(0, ITEMS_PER_PAGE)
  );

  useEffect(() => {
    setPage(1);
    setTotalPages(Math.max(Math.ceil(data.length / ITEMS_PER_PAGE), 1));
  }, [data]);

  useEffect(() => {
    setPaginatedData(
      data.slice((page - 1) * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
    );
  }, [data, page]);

  const changePage = (page: number) => {
    if (page < 1) {
      page = 1;
    }
    if (page > totalPages) {
      page = totalPages;
    }
    setPage(page);
  };

  return {
    page,
    totalPages,
    changePage,
    paginatedData,
  };
};

export default usePagination;
