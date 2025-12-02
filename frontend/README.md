# EditorManager Frontend

EditorManager Frontend is an Angular application that provides a user-friendly interface for managing Authors, Books, Magazines, and Publications. Built with Angular 19 and PrimeNG components.

---

## Features

- **Modern Angular 19** application with TypeScript
- **PrimeNG UI components** for rich, accessible interfaces
- **Responsive design** with PrimeFlex CSS utilities
- **REST API integration** with the backend service
- **Form validation** and error handling
- **Proxy configuration** for seamless API communication
- **Hot-reload development** server

---

## Prerequisites

- **Node.js 18+** and **npm** (or **yarn**)
- **Angular CLI 19+** (installed globally or via npx)
- **Backend API** running on `http://localhost:8080` (or configured endpoint)

---

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/          # Angular components
│   │   ├── services/            # API services
│   │   ├── models/              # TypeScript models/interfaces
│   │   ├── guards/              # Route guards
│   │   └── app.component.*      # Root component
│   ├── environments/            # Environment configurations
│   ├── assets/                  # Static assets
│   ├── styles.scss              # Global styles
│   └── index.html               # Entry HTML
├── angular.json                  # Angular configuration
├── package.json                 # Dependencies
├── proxy.conf.json              # API proxy configuration
└── tsconfig.json                # TypeScript configuration
```

---

## Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

Or if using yarn:

```bash
yarn install
```

### 2. Environment Configuration

Configure environment files in `src/environments/`:

**`src/environments/environment.ts`** (development):
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

**`src/environments/environment.prod.ts`** (production):
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-api-domain.com'
};
```

### 3. Proxy Configuration

The `proxy.conf.json` file is configured to proxy API requests:

```json
{
  "/api/*": {
    "target": "http://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "pathRewrite": {
      "^/api": ""
    }
  }
}
```

For local development, you may need to update the target to `http://localhost:8080`.

---

## Running the Application

### Option 1: Local Development Server

Start the development server with hot-reload:

```bash
npm start
```

Or:

```bash
ng serve
```

The application will be available at: **http://localhost:4200**

The server will automatically reload when you modify source files.

### Option 2: Using Docker Compose

From the project root:

```bash
docker-compose -f docker-compose.dev.yml up frontend
```

### Option 3: Production Build

Build for production:

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory. The production build optimizes the application for performance and speed.

To serve the production build locally:

```bash
npx http-server dist/editor-manager-frontend
```

---

## Development

### Code Scaffolding

Angular CLI provides powerful code generation tools:

**Generate a new component:**
```bash
ng generate component component-name
```

**Generate a new service:**
```bash
ng generate service service-name
```

**Generate a new module:**
```bash
ng generate module module-name
```

For a complete list of available schematics:

```bash
ng generate --help
```

### Available Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Start development server |
| `npm run build` | Build for production |
| `npm run watch` | Build and watch for changes |
| `npm test` | Run unit tests |

---

## Testing

### Unit Tests

Run unit tests with Karma:

```bash
npm test
```

Or:

```bash
ng test
```

Tests will execute in watch mode by default. Press `Ctrl+C` to exit.

### End-to-End Tests

For E2E testing, the project uses separate test modules:
- **e2e-tests/** - Playwright + Cucumber E2E tests
- **e2e-selenium/** - Selenium + TestNG E2E tests

See the respective README files in those directories.

---

## Building

### Development Build

```bash
npm run build
```

### Production Build

```bash
ng build --configuration production
```

### Build Output

The build process:
- Compiles TypeScript to JavaScript
- Bundles and minifies code
- Optimizes assets
- Generates source maps (for debugging)

Output directory: `dist/editor-manager-frontend/`

---

## Styling

The project uses:
- **PrimeNG** - UI component library
- **PrimeFlex** - CSS utility framework
- **PrimeIcons** - Icon library
- **SCSS** - Styling preprocessor

Global styles are defined in `src/styles.scss`.

---

## API Integration

The frontend communicates with the backend API through:

1. **Services** - Angular services that make HTTP requests
2. **Proxy Configuration** - Development proxy for API calls
3. **Environment Variables** - API URL configuration

Example service usage:

```typescript
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAuthors(): Observable<Author[]> {
    return this.http.get<Author[]>(`${this.apiUrl}/authors`);
  }
}
```

---

## Configuration

### Angular Configuration

Key settings in `angular.json`:
- **Output path**: `dist/editor-manager-frontend`
- **Development server port**: `4200`
- **Proxy configuration**: `proxy.conf.json`
- **Source maps**: Enabled for development

### TypeScript Configuration

TypeScript settings in `tsconfig.json`:
- **Target**: ES2022
- **Module**: ES2022
- **Strict mode**: Enabled

---

## Troubleshooting

### Port Already in Use

If port 4200 is already in use:

```bash
ng serve --port 4201
```

### Build Errors

- Clear cache: `rm -rf node_modules .angular dist`
- Reinstall dependencies: `npm install`
- Check Node.js version: `node --version` (should be 18+)

### API Connection Issues

- Verify backend is running: `curl http://localhost:8080/api/authors`
- Check proxy configuration in `proxy.conf.json`
- Verify CORS settings in backend
- Check browser console for errors

### Module Not Found Errors

- Clear node_modules: `rm -rf node_modules`
- Reinstall: `npm install`
- Check package.json for correct dependencies

---

## Dependencies

### Key Dependencies

- **@angular/core** ^19.2.0 - Angular framework
- **@angular/router** ^19.2.0 - Routing
- **@angular/forms** ^19.2.0 - Forms and validation
- **primeng** ^20.0.0 - UI component library
- **primeflex** ^4.0.0 - CSS utilities
- **rxjs** ~7.8.0 - Reactive programming

### Dev Dependencies

- **@angular/cli** ^19.2.15 - Angular CLI
- **@angular/compiler-cli** ^19.2.0 - TypeScript compiler
- **typescript** ~5.7.2 - TypeScript compiler
- **karma** ~6.4.0 - Test runner
- **jasmine-core** ~5.6.0 - Testing framework

---

## Browser Support

The application supports:
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

---

## Performance Optimization

- **Lazy loading** - Routes are lazy-loaded for better performance
- **OnPush change detection** - Optimized change detection strategy
- **Tree shaking** - Unused code is eliminated in production builds
- **Code splitting** - Automatic code splitting for optimal bundle sizes

---

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests: `npm test`
4. Build the project: `npm run build`
5. Submit a pull request

---

## License

MIT License
