import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import tailwindcss from "@tailwindcss/vite";
import { AntDesignVueResolver } from "unplugin-vue-components/resolvers";

// https://vite.dev/config/
export default defineConfig({
  base: "./",
  plugins: [
    vue(),
    tailwindcss(),
    AutoImport({
      imports: ["vue", "vue-router"],
      dts: "config/auto-import.d.ts",
    }),
    Components({
      dirs: ["src/components", "src/views"],
      extensions: ["vue"],
      dts: "config/components.d.ts",
      include: [/\.vue$/, /\.vue\?vue/],
      exclude: [
        /[\\/]node_modules[\\/]/,
        /[\\/]\.git[\\/]/,
        /[\\/]\.nuxt[\\/]/,
      ],
      resolvers: [
        AntDesignVueResolver({
          importStyle: false,
        }),
      ],
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    proxy: {
      "/draw-api": {
        target: "http://localhost:8866",
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/draw-api/, ""),
      },
      "/draw-socket": {
        target: "http://localhost:9000",
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/draw-socket/, ""),
      },
    },
  },
});
