import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Home } from '../features/home/Home';
import Login from '../features/auth/Login';
import { Profile } from '../features/app/profile/Profile';
import { useEffect, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { PrivateRoute } from './PrivateRoute';

export const AppRouter = () => {

    const { fetchProfile } = useAuth();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initAuth = async () => {
            try {
                await fetchProfile();
            } catch {
                console.log("No autenticado");
            } finally {
                setLoading(false);
            }
        };

        initAuth();
    }, []);

    if (loading) {
        return <div>Cargando...</div>;
    }

    const renderMultiRoutes = ({ element: Element, paths, ...rest }: { element: React.ReactElement; paths: string[];[key: string]: unknown }) => paths.map((path: string) => {
        return {
            ...rest,
            path, element: Element
        }
    });

    const router = createBrowserRouter([
        ...renderMultiRoutes({ paths: ['/home', '/'], element: <Home /> }),
        { path: '/login', element: <Login /> },
        { path: '/profile', element: <PrivateRoute><Profile /></PrivateRoute> },
    ]);

    return <RouterProvider router={router} />;
};