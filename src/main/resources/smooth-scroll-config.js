const scroll = new SmoothScroll('a[href*="#"]', {
    speed: 300,
    offset: 100,
    updateURL: true,
    popstate: true,
    emitEvents: true
});

let BeforeEvent;
document.addEventListener(
    "scrollStart",
    event => {
        if (BeforeEvent !== undefined) {
            BeforeEvent.detail.anchor.classList.remove("link-target");
        }
        event.detail.anchor.classList.add("link-target");
        BeforeEvent = event;
    },
    false
);