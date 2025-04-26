import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Home } from '../features/home/Home';

export const AppRouter = () => {

    const renderMultiRoutes = ({ element: Element, paths, ...rest }: { element: React.ReactElement; paths: string[];[key: string]: unknown }) => paths.map((path: string) => {
        return {
            ...rest,
            path, element: Element
        }
    });

    const router = createBrowserRouter([
        ...renderMultiRoutes({ paths: ['/home', '/'], element: <Home /> })
    ]);

    return <RouterProvider router={router} />;
};