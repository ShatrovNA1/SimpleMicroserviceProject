#!/bin/bash

# ===========================================
# E-Commerce Microservices - Start Script
# ===========================================

set -e

echo "🚀 E-Commerce Microservices Startup Script"
echo "==========================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    echo -n "Waiting for $service_name"
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo ""
            print_status "$service_name is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo ""
    print_error "$service_name failed to start"
    return 1
}

# Check Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker."
    exit 1
fi

# Parse arguments
MODE=${1:-"all"}

case $MODE in
    "infra")
        echo ""
        print_status "Starting infrastructure only..."
        docker-compose -f docker-compose.infra.yml up -d

        echo ""
        print_status "Infrastructure services:"
        echo "  - PostgreSQL (5432-5436)"
        echo "  - Kafka (9092, 29092)"
        echo "  - Zookeeper (2181)"
        echo "  - Zipkin (9411)"
        echo "  - Mailhog (8025)"

        echo ""
        print_status "Now you can start services locally:"
        echo "  ./gradlew :eureka-server:bootRun"
        echo "  ./gradlew :config-server:bootRun"
        echo "  ./gradlew :api-gateway:bootRun"
        echo "  ... etc"
        ;;

    "all")
        echo ""
        print_status "Building and starting all services..."
        docker-compose up --build -d

        echo ""
        print_status "Waiting for services to start..."

        wait_for_service "http://localhost:8761/actuator/health" "Eureka Server"
        wait_for_service "http://localhost:8888/actuator/health" "Config Server"
        wait_for_service "http://localhost:8080/actuator/health" "API Gateway"

        echo ""
        print_status "All services started!"
        echo ""
        echo "📊 Service URLs:"
        echo "  - API Gateway:    http://localhost:8080"
        echo "  - Eureka:         http://localhost:8761"
        echo "  - Config Server:  http://localhost:8888"
        echo "  - Zipkin:         http://localhost:9411"
        echo "  - Mailhog:        http://localhost:8025"
        ;;

    "stop")
        echo ""
        print_status "Stopping all services..."
        docker-compose down
        print_status "All services stopped."
        ;;

    "restart")
        echo ""
        print_status "Restarting all services..."
        docker-compose restart
        print_status "All services restarted."
        ;;

    "logs")
        SERVICE=${2:-""}
        if [ -z "$SERVICE" ]; then
            docker-compose logs -f
        else
            docker-compose logs -f "$SERVICE"
        fi
        ;;

    "status")
        echo ""
        print_status "Service Status:"
        docker-compose ps
        ;;

    "clean")
        echo ""
        print_warning "This will remove all containers and volumes!"
        read -p "Are you sure? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker-compose down -v
            print_status "Cleanup complete."
        fi
        ;;

    *)
        echo ""
        echo "Usage: $0 {all|infra|stop|restart|logs|status|clean}"
        echo ""
        echo "Commands:"
        echo "  all     - Start all services (default)"
        echo "  infra   - Start only infrastructure (databases, kafka, etc.)"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show logs (optional: service name)"
        echo "  status  - Show service status"
        echo "  clean   - Stop and remove all data"
        echo ""
        echo "Examples:"
        echo "  $0 all"
        echo "  $0 infra"
        echo "  $0 logs user-service"
        exit 1
        ;;
esac

echo ""
print_status "Done!"

