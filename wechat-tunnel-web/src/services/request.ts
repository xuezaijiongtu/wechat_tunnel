import axios from 'axios';

const request = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL || '/api',
    timeout: 15000,
});

request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

request.interceptors.response.use(
    (response) => {
        const wrapper = response.data;
        console.log('响应拦截器收到数据:', wrapper);
        // Unwrap ResponseWrapper: { success, code, message, data }
        if (wrapper && typeof wrapper === 'object' && 'success' in wrapper) {
            if (!wrapper.success) {
                return Promise.reject(new Error(wrapper.message || '请求失败'));
            }
            console.log('返回 wrapper.data:', wrapper.data);
            return Promise.resolve(wrapper.data);
        }
        console.log('直接返回 wrapper:', wrapper);
        return Promise.resolve(wrapper);
    },
    (error) => {
        console.error('响应拦截器错误:', error);
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default request;
