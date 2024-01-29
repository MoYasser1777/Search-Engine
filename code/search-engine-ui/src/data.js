const data = [];

for (let i = 1; i <= 50; i++) {
    data.push({
        title: `Website ${i}`,
        url: `https://www.website${i}.com`,
        queryParagraph: `Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed euismod augue vitae lorem ultrices luctus. Proin euismod, augue at aliquam dignissim, sapien lorem tincidunt arcu, a bibendum mi eros sit amet mi.`
    });
}

export default data;
