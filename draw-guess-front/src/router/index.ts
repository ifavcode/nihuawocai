import { createRouter, createWebHashHistory } from "vue-router";
import IndexView from "../views/index/index.vue";
import TestPage from "@/views/TestPage/index.vue";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "index",
      redirect: "/index",
      children: [
        {
          path: "/index",
          component: IndexView,
        },
        {
          path: "/test",
          component: TestPage,
        },
      ],
    },
  ],
});

export default router;
