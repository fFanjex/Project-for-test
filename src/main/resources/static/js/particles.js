const canvas = document.getElementById('bgCanvas');
const ctx = canvas.getContext('2d');
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

let particlesArray = [];

class Particle {
    constructor(x, y, size, speedX, speedY, color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speedX = speedX;
        this.speedY = speedY;
        this.color = color;
    }

    update() {
        this.x += this.speedX;
        this.y += this.speedY;

        if(this.x < 0 || this.x > canvas.width) this.speedX = -this.speedX;
        if(this.y < 0 || this.y > canvas.height) this.speedY = -this.speedY;
    }

    draw() {
        ctx.shadowBlur = 15;
        ctx.shadowColor = this.color;
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI*2);
        ctx.fill();
    }
}

function init() {
    particlesArray = [];
    for(let i=0; i<150; i++) {
        let size = Math.random()*3 + 1;
        let x = Math.random()*canvas.width;
        let y = Math.random()*canvas.height;
        let speedX = (Math.random() - 0.5)*1;
        let speedY = (Math.random() - 0.5)*1;
        let color = `hsl(${Math.random()*360}, 100%, 50%)`;
        particlesArray.push(new Particle(x, y, size, speedX, speedY, color));
    }
}

function animate() {
    ctx.clearRect(0,0,canvas.width, canvas.height);
    particlesArray.forEach(p => {p.update(); p.draw();});
    requestAnimationFrame(animate);
}

window.addEventListener('resize', function() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    init();
});

init();
animate();