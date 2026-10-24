import React from 'react';
import ReactMarkdown from 'react-markdown';

/**
 * Pre-processes streaming text to ensure:
 * 1. Unclosed `**` during real-time streaming are closed so they never render as literal `**`.
 * 2. Unicode bullets `•` are normalized to standard markdown `- ` for semantic list rendering.
 */
function cleanMarkdownText(content, isStreaming) {
  if (!content) return '';

  let cleaned = content;

  // Convert unicode bullets `•` into standard markdown list items `- `
  cleaned = cleaned.replace(/^[ \t]*•[ \t]*/gm, '- ');

  // If streaming and there is an odd count of `**`, auto-close it so raw asterisks are never visible
  if (isStreaming) {
    const boldMatches = cleaned.match(/\*\*/g);
    if (boldMatches && boldMatches.length % 2 !== 0) {
      cleaned += '**';
    }
    // Also clean dangling single asterisks at the very end
    if (cleaned.endsWith('*') && !cleaned.endsWith('**')) {
      cleaned = cleaned.slice(0, -1);
    }
  }

  return cleaned;
}

export default function StreamingMarkdown({ content, isStreaming = false }) {
  const processedText = cleanMarkdownText(content, isStreaming);

  return (
    <div className="prose prose-sm max-w-none text-gray-800 font-sans leading-relaxed break-words">
      <ReactMarkdown
        components={{
          // Render bold as real, crisp bold text - NO asterisks!
          strong: ({ node, ...props }) => (
            <strong className="font-extrabold text-gray-950 inline" {...props} />
          ),
          b: ({ node, ...props }) => (
            <b className="font-extrabold text-gray-950 inline" {...props} />
          ),
          // Render italics
          em: ({ node, ...props }) => (
            <em className="italic text-gray-800" {...props} />
          ),
          // Paragraphs
          p: ({ node, ...props }) => (
            <p className="mb-2.5 last:mb-0 leading-relaxed" {...props} />
          ),
          // Lists
          ul: ({ node, ...props }) => (
            <ul className="my-2 space-y-1 pl-4 list-disc marker:text-emerald-600" {...props} />
          ),
          ol: ({ node, ...props }) => (
            <ol className="my-2 space-y-1 pl-4 list-decimal marker:text-emerald-700 font-medium" {...props} />
          ),
          li: ({ node, ...props }) => (
            <li className="leading-relaxed pl-1" {...props} />
          ),
          // Code & dosages
          code: ({ node, inline, ...props }) => (
            <code
              className="bg-emerald-50 text-emerald-950 border border-emerald-200 px-1.5 py-0.5 rounded text-[11px] font-mono font-bold"
              {...props}
            />
          ),
          // Headings
          h1: ({ node, children, ...props }) => (
            <h1 className="text-base font-black text-emerald-950 mb-2 mt-3" {...props}>{children}</h1>
          ),
          h2: ({ node, children, ...props }) => (
            <h2 className="text-sm font-extrabold text-emerald-900 mb-1.5 mt-2.5" {...props}>{children}</h2>
          ),
          h3: ({ node, children, ...props }) => (
            <h3 className="text-xs font-bold text-emerald-800 mb-1 mt-2 uppercase tracking-wide" {...props}>{children}</h3>
          ),
          // Blockquotes
          blockquote: ({ node, ...props }) => (
            <blockquote
              className="border-l-4 border-emerald-500 pl-3 py-1 my-2 bg-emerald-50/60 rounded-r text-emerald-900 italic text-xs"
              {...props}
            />
          ),
        }}
      >
        {processedText}
      </ReactMarkdown>
    </div>
  );
}
