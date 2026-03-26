const fs = require('fs');
const path = require('path');

const srcDir = 'c:\\Users\\dibag\\OneDrive\\Documents\\ALL PROJECT\\faculty-load-analyzer\\frontend\\src\\pages';

const configs = [
    { file: 'DepartmentPage.js', endpoint: '/department/${deleteId}' },
    { file: 'CoursePage.js', endpoint: '/course-admin/courses/${deleteId}' },
    { file: 'VenuePage.js', endpoint: '/venue-admin/venues/${deleteId}' },
    { file: 'FacultyAdminPage.js', endpoint: '/faculty-admin/faculties/${deleteId}' },
    { file: 'SuperAdminPage.js', endpoint: '/super-admin/admins/${deleteId}' },
    { file: 'CourseMappingPage.js', endpoint: '/hod/mappings/${deleteId}' },
    { file: 'TimetablePage.js', endpoint: '/hod/timetables/${deleteId}' }
];

for (const config of configs) {
    const filePath = path.join(srcDir, config.file);
    if (!fs.existsSync(filePath)) continue;

    let content = fs.readFileSync(filePath, 'utf8');

    // 1. Add import if not exists
    if (!content.includes('ConfirmDialog')) {
        content = content.replace(/(import .* from '@mui\/icons-material';)/, "$1\nimport ConfirmDialog from '../components/ConfirmDialog';");
    }

    // 2. Add state
    if (!content.includes('confirmOpen')) {
        content = content.replace(/(const \[error, setError\] = useState\(''\);)/, "$1\n    const [confirmOpen, setConfirmOpen] = useState(false);\n    const [deleteId, setDeleteId] = useState(null);");
    }

    // 3. Replace handleDelete
    const deleteRegex = /const handleDelete = async \(id\) => \{ if \(window\.confirm\('.*?'\)\) \{ await api\.delete\(`.*?`\); fetchData\(\); \} \};/g;
    const deleteRegex2 = /const handleDelete = async \(id\) => \{\s*if \(window\.confirm\('.*?'\)\) \{ await api\.delete\(`.*?`\); fetchData\(\); \}\s*\};/g;

    const newDeleteLogic = `const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(\`${config.endpoint}\`); fetchData(); } catch { setError('Failed to delete'); }
        setConfirmOpen(false); setDeleteId(null);
    };`;

    if (content.match(deleteRegex) || content.match(deleteRegex2)) {
        content = content.replace(deleteRegex, newDeleteLogic);
        content = content.replace(deleteRegex2, newDeleteLogic);
    }

    // 4. Update JSX onClick
    content = content.replace(/(<IconButton onClick=\{\(\) => )handleDelete((?:\(d\.id\)|\(c\.id\)|\(v\.id\)|\(f\.id\)|\(m\.id\)|\(a\.id\)|\(t\.id\))\})/, "$1handleDeleteClick$2");

    // 5. Add Dialog component right before the last </Box>
    if (!content.includes('<ConfirmDialog')) {
        const dialogJsx = `
                <ConfirmDialog 
                    open={confirmOpen} 
                    title="Confirm Deletion" 
                    content="Are you sure you want to delete this item? This action cannot be undone." 
                    onConfirm={handleConfirmDelete} 
                    onCancel={() => setConfirmOpen(false)} 
                    confirmText="Delete" 
                />
            </Box>
        </Box>`;
        content = content.replace(/<\/Box>\s*<\/Box>\s*\);\s*};\s*export default/, dialogJsx + "\n    );\n};\n\nexport default");
    }

    fs.writeFileSync(filePath, content, 'utf8');
}
console.log('Done replacement');
