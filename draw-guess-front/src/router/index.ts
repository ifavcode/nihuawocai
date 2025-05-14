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
          name: 'room',
          component: IndexView,
        },
        {
          name: 'hall',
          path: "/hall",
          component: () => import('@/views/hall/index.vue'),
          meta: {
            title: '你画我猜'
          }
        },
        {
          path: "/test",
          component: TestPage,
          meta: {
            title: '测试页'
          }
        },
      ],
    },
  ],
});

router.beforeEach((to) => {
  if (to.meta.title) {
    document.title = to.meta.title as string
  }
})

export default router;
