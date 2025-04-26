import { Provider } from 'jotai';
import { Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import { AppRouter } from './routes/AppRouter.tsx';
import './styles/index.css';

createRoot(document.getElementById('root')!).render(
  <Suspense fallback={<></>}>
    <Provider>
      <AppRouter />
    </Provider>
  </Suspense>
);